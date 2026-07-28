import { useEffect, useRef, useState } from "react";
import * as THREE from "three";
import { parseItemModel, type ItemModelElement, type ItemModelFace } from "./ItemModelParser";
import { MINECRAFT_DIRECTIONS, minecraftFaceVertices, normalizedMinecraftUv, type MinecraftDirection } from "./minecraft-face";
import { resolveModelTexture, type TextureResolution } from "./MinecraftTextureResolver";
import type { AssetStore } from "./lib/asset-store";
import { installViewportControls } from "./viewport-controls";
import { Icon } from "./Icon";
import {
  isMinecraftTextureResource,
  minecraftRemoteAssets
} from "./lib/minecraft-remote-assets";

interface ItemModelRendererProps {
  store: AssetStore;
  modelPath: string;
  texturePath?: string;
  textureSlots?: Record<string, string>;
  context?: string;
  interactive?: boolean;
  displayTransform?: "game" | "editor";
  fitScale?: number;
  textureAnimation?: { frames: string[]; frame_duration: number; trigger?: string; interval?: { min: number; max: number } };
  previewYaw?: number;
}

function missingTexture(): THREE.DataTexture {
  const size = 16;
  const pixels = new Uint8Array(size * size * 4);
  for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) {
    const offset = (y * size + x) * 4;
    const dark = ((x >> 2) + (y >> 2)) % 2 === 0;
    pixels.set(dark ? [30, 0, 30, 255] : [255, 0, 255, 255], offset);
  }
  const texture = new THREE.DataTexture(pixels, size, size, THREE.RGBAFormat);
  texture.needsUpdate = true;
  texture.magFilter = THREE.NearestFilter;
  texture.minFilter = THREE.NearestFilter;
  texture.colorSpace = THREE.SRGBColorSpace;
  return texture;
}

function bounds(element: ItemModelElement) {
  return {
    minX: element.from[0], minY: element.from[1], minZ: element.from[2],
    maxX: element.to[0], maxY: element.to[1], maxZ: element.to[2]
  };
}

export function ItemModelRenderer({
  store,
  modelPath,
  texturePath,
  textureSlots = {},
  context = "gui"
  , interactive = true
  , displayTransform = "game"
  , fitScale = 2.4
  , textureAnimation
  , previewYaw = 0
}: ItemModelRendererProps) {
  const host = useRef<HTMLDivElement>(null);
  const [error, setError] = useState("");
  const [warnings, setWarnings] = useState<string[]>([]);
  const stableSlots = JSON.stringify(textureSlots);

  useEffect(() => {
    if (!host.current) return;
    const container = host.current;
    setError("");
    setWarnings([]);

    const scene = new THREE.Scene();
    scene.background = new THREE.Color("#101512");
    const camera = new THREE.PerspectiveCamera(32, 1, 0.01, 100);
    camera.position.set(1.35, 1.15, 2.15);
    camera.lookAt(0, 0, 0);
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false });
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
    container.prepend(renderer.domElement);

    scene.add(new THREE.HemisphereLight(0xffffff, 0x243126, 2.7));
    const light = new THREE.DirectionalLight(0xffe0a2, 2);
    light.position.set(2, 3, 2);
    scene.add(light);

    const viewRoot = new THREE.Group();
    const modelRoot = new THREE.Group();
    modelRoot.position.set(-0.5, -0.5, -0.5);
    viewRoot.add(modelRoot);
    scene.add(viewRoot);
    const grid = new THREE.GridHelper(4, 16, 0x6a756c, 0x354139);
    grid.position.y = -0.5;
    (grid.material as THREE.Material).transparent = true;
    (grid.material as THREE.Material).opacity = 0.42;
    if (interactive) scene.add(grid);
    const axes = new THREE.AxesHelper(0.7);
    axes.position.y = -0.495;
    if (interactive) scene.add(axes);

    const objectUrls: string[] = [];
    const ownedTextures = new Set<THREE.Texture>();
    const animatedTextures = new Map<string, THREE.Texture>();
    const animatedMaterials: THREE.Material[] = [];
    let activeTextureFrame = -1;
    let textureLastTick = 0;
    let texturePlaybackTick = 0;
    let textureWaiting = textureAnimation?.trigger === "random_idle";
    let textureWaitTicks = textureAnimation?.interval
      ? textureAnimation.interval.min + Math.floor(Math.random() * (textureAnimation.interval.max - textureAnimation.interval.min + 1))
      : 0;
    const fallback = missingTexture();
    ownedTextures.add(fallback);
    let disposed = false;

    const loadTexture = async (resolution: TextureResolution): Promise<THREE.Texture> => {
      if (resolution.kind !== "internal") return fallback;
      const value = await store.read(resolution.path);
      const url = typeof value === "string" ? value : URL.createObjectURL(value);
      if (typeof value !== "string") objectUrls.push(url);
      const texture = await new THREE.TextureLoader().loadAsync(url);
      texture.flipY = false;
      texture.magFilter = THREE.NearestFilter;
      texture.minFilter = THREE.NearestFilter;
      texture.colorSpace = THREE.SRGBColorSpace;
      ownedTextures.add(texture);
      return texture;
    };

    const loadExternalTexture = async (resource: string): Promise<THREE.Texture | null> => {
      const value = await minecraftRemoteAssets.resolve(resource);
      if (!value) return null;
      const url = URL.createObjectURL(value);
      objectUrls.push(url);
      const texture = await new THREE.TextureLoader().loadAsync(url);
      texture.flipY = false;
      texture.magFilter = THREE.NearestFilter;
      texture.minFilter = THREE.NearestFilter;
      texture.colorSpace = THREE.SRGBColorSpace;
      ownedTextures.add(texture);
      return texture;
    };

    const makeFace = (
      element: ItemModelElement,
      direction: MinecraftDirection,
      face: ItemModelFace,
      origin: [number, number, number],
      texture: THREE.Texture
    ) => {
      const positions = minecraftFaceVertices(bounds(element), direction)
        .flatMap(([x, y, z]) => [(x - origin[0]) / 16, (y - origin[1]) / 16, (z - origin[2]) / 16]);
      const uvs = [0, 1, 2, 3]
        .flatMap((vertex) => normalizedMinecraftUv(face.uv, face.rotation, vertex, 16, 16));
      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
      geometry.setAttribute("uv", new THREE.Float32BufferAttribute(uvs, 2));
      geometry.setIndex([0, 1, 2, 0, 2, 3]);
      geometry.computeVertexNormals();
      const materialOptions = {
        map: texture,
        transparent: true,
        alphaTest: 0.01,
        side: THREE.DoubleSide
      };
      const material = element.shade === false
        ? new THREE.MeshBasicMaterial(materialOptions)
        : new THREE.MeshLambertMaterial(materialOptions);
      return new THREE.Mesh(geometry, material);
    };

    const controls = interactive ? installViewportControls(camera, renderer.domElement, modelRoot) : null;
    const load = async () => {
      const model = await parseItemModel(store, modelPath);
      const slots = { ...textureSlots };
      if (texturePath) slots.base = texturePath;

      const resolutions = new Map<string, TextureResolution>();
      for (const element of model.elements) {
        for (const direction of MINECRAFT_DIRECTIONS) {
          const face = element.faces[direction];
          if (!face) continue;
          const key = face.texture || "#base";
          if (!resolutions.has(key)) {
            resolutions.set(key, resolveModelTexture(key, model.textures, slots, store));
          }
        }
      }

      const textureCache = new Map<string, THREE.Texture>();
      const warningMessages: string[] = [];
      for (const [key, resolution] of resolutions) {
        if (resolution.kind === "external") {
          const external = await loadExternalTexture(resolution.resource);
          if (external) textureCache.set(key, external);
          else {
            warningMessages.push(`无法下载 Minecraft 纹理：${resolution.resource}（${key}）`);
            textureCache.set(key, fallback);
          }
        } else if (resolution.kind === "missing") {
          warningMessages.push(resolution.reason);
          textureCache.set(key, fallback);
        } else {
          try {
            textureCache.set(key, await loadTexture(resolution));
          } catch {
            warningMessages.push(`无法读取纹理：${resolution.path}（${key}）`);
            textureCache.set(key, fallback);
          }
        }
      }
      if (!disposed) setWarnings([...new Set(warningMessages)]);

      if (textureAnimation) {
        for (const slot of new Set(textureAnimation.frames)) {
          const path = textureSlots[slot];
          if (!path) continue;
          try {
            const value = isMinecraftTextureResource(path)
              ? await minecraftRemoteAssets.resolve(path)
              : await store.read(path);
            if (!value) throw new Error(`无法下载 Minecraft 纹理：${path}`);
            const url = typeof value === "string" ? value : URL.createObjectURL(value);
            if (typeof value !== "string") objectUrls.push(url);
            const animated = await new THREE.TextureLoader().loadAsync(url);
            animated.flipY = false;
            animated.magFilter = THREE.NearestFilter;
            animated.minFilter = THREE.NearestFilter;
            animated.colorSpace = THREE.SRGBColorSpace;
            animatedTextures.set(slot, animated);
            ownedTextures.add(animated);
          } catch {
            // Keep the model texture and warning state when an animation frame is missing.
          }
        }
      }

      for (const element of model.elements) {
        const rotation = element.rotation;
        const origin = rotation?.origin ?? [8, 8, 8];
        const group = new THREE.Group();
        group.position.set(origin[0] / 16, origin[1] / 16, origin[2] / 16);
        if (rotation?.axis && rotation.angle) {
          group.rotation[rotation.axis] = THREE.MathUtils.degToRad(rotation.angle);
        }
        for (const direction of MINECRAFT_DIRECTIONS) {
          const face = element.faces[direction];
          if (face) group.add(makeFace(element, direction, face, origin, textureCache.get(face.texture || "#base") ?? fallback));
        }
        modelRoot.add(group);
      }
      modelRoot.traverse((object) => {
        if (!(object instanceof THREE.Mesh)) return;
        const materials = Array.isArray(object.material) ? object.material : [object.material];
        animatedMaterials.push(...materials);
      });

      const display = displayTransform === "game" ? model.display[context] : undefined;
      if (display) {
        const rotation = display.rotation ?? [0, 0, 0];
        const translation = display.translation ?? [0, 0, 0];
        const scale = display.scale ?? [1, 1, 1];
        modelRoot.rotation.set(
          THREE.MathUtils.degToRad(rotation[0]),
          THREE.MathUtils.degToRad(rotation[1]),
          THREE.MathUtils.degToRad(rotation[2])
        );
        modelRoot.position.add(new THREE.Vector3(
          translation[0] / 16,
          translation[1] / 16,
          translation[2] / 16
        ));
        modelRoot.scale.set(...scale);
      }
      const box = new THREE.Box3().setFromObject(modelRoot);
      const center = box.getCenter(new THREE.Vector3());
      const size = box.getSize(new THREE.Vector3());
      const distance = Math.max(size.x, size.y, size.z, 0.5) * fitScale;
      const direction = displayTransform === "editor" ? -1 : 1;
      const offset = new THREE.Vector3(distance * 0.7, distance * 0.45, distance * direction);
      offset.applyAxisAngle(new THREE.Vector3(0, 1, 0), THREE.MathUtils.degToRad(previewYaw));
      const position = center.clone().add(offset);
      if (controls) controls.setView(center, position);
      else { camera.position.copy(position); camera.lookAt(center); }
    };

    load().catch((reason) => {
      if (!disposed) setError(reason instanceof Error ? reason.message : "模型加载失败");
    });

    let raf = 0;
    const draw = () => {
      if (textureAnimation?.frames.length && animatedMaterials.length) {
        const currentTick = performance.now() / 50;
        const elapsedTicks = Math.max(0, Math.floor(currentTick) - Math.floor(textureLastTick));
        textureLastTick = currentTick;
        const duration = Math.max(1, textureAnimation.frame_duration);
        if (textureAnimation.trigger === "random_idle") {
          for (let tick = 0; tick < elapsedTicks; tick++) {
            if (textureWaiting) {
              if (--textureWaitTicks <= 0) { textureWaiting = false; texturePlaybackTick = 0; activeTextureFrame = -1; }
            } else {
              texturePlaybackTick++;
              if (texturePlaybackTick >= duration * textureAnimation.frames.length) {
                textureWaiting = true;
                textureWaitTicks = textureAnimation.interval
                  ? textureAnimation.interval.min + Math.floor(Math.random() * (textureAnimation.interval.max - textureAnimation.interval.min + 1))
                  : 80;
              }
            }
          }
        } else if (textureAnimation.trigger === "manual" || textureAnimation.trigger === "on_totem_activate") {
          textureWaiting = true;
        } else {
          texturePlaybackTick = currentTick;
        }
        if (textureWaiting) { renderer.render(scene, camera); raf = requestAnimationFrame(draw); return; }
        const index = Math.floor(texturePlaybackTick / duration) % textureAnimation.frames.length;
        if (index !== activeTextureFrame) {
          const texture = animatedTextures.get(textureAnimation.frames[index]);
          if (texture) animatedMaterials.forEach((material) => {
            if ("map" in material) {
              (material as THREE.MeshBasicMaterial | THREE.MeshLambertMaterial).map = texture;
              material.needsUpdate = true;
            }
          });
          activeTextureFrame = index;
        }
      }
      renderer.render(scene, camera);
      raf = requestAnimationFrame(draw);
    };
    draw();

    const reset = () => controls?.reset();
    if (controls) container.addEventListener("model-preview-reset", reset);

    const resize = () => {
      const width = Math.max(1, container.clientWidth);
      const height = Math.max(1, container.clientHeight);
      renderer.setSize(width, height);
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
    };
    const observer = new ResizeObserver(resize);
    observer.observe(container);
    resize();

    return () => {
      disposed = true;
      cancelAnimationFrame(raf);
      observer.disconnect();
      if (controls) {
        container.removeEventListener("model-preview-reset", reset);
        controls.dispose();
      }
      scene.traverse((object) => {
        if (!(object instanceof THREE.Mesh)) return;
        object.geometry.dispose();
        const materials = Array.isArray(object.material) ? object.material : [object.material];
        materials.forEach((material) => material.dispose());
      });
      ownedTextures.forEach((texture) => texture.dispose());
      objectUrls.forEach(URL.revokeObjectURL);
      renderer.dispose();
      renderer.domElement.remove();
    };
  }, [store, modelPath, texturePath, stableSlots, context, interactive, displayTransform, fitScale, previewYaw, JSON.stringify(textureAnimation)]);

  return (
    <div className="model-viewport item-preview" ref={host}>
      {error && <div className="preview-message error">{error}</div>}
      {interactive && !error && warnings.length > 0 && (
        <div className="preview-message warning" title={warnings.join("\n")}>
          资源警告 {warnings.length}
        </div>
      )}
      {interactive && <button
        className="viewport-reset"
        type="button"
        onClick={() => host.current?.dispatchEvent(new Event("model-preview-reset"))}
      >
        <Icon name="reset" />
      </button>}
    </div>
  );
}
