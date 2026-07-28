import { useEffect, useRef, useState } from "react";
import * as THREE from "three";
import type { AssetStore } from "./lib/asset-store";
import {
  MINECRAFT_DIRECTIONS,
  boneUvSize,
  minecraftFaceVertices,
  normalizedMinecraftUv,
  type FaceUv,
  type MinecraftDirection
} from "./minecraft-face";
import { installViewportControls } from "./viewport-controls";
import { interpolateBoneFrames, resolveAnimationName } from "./lib/animation-utils";
import { Icon } from "./Icon";
import {
  isMinecraftTextureResource,
  minecraftRemoteAssets
} from "./lib/minecraft-remote-assets";

type Frame = { time: number; value: [number, number, number]; interpolation?: string };
type Timeline = { rotation?: Frame[] | Frame; position?: Frame[] | Frame; scale?: Frame[] | Frame };
type Face = { uv: FaceUv; rotation?: number };
type Cube = {
  origin: [number, number, number];
  size: [number, number, number];
  uv?: [number, number];
  mirror?: boolean;
  faces?: Partial<Record<MinecraftDirection, Face>>;
};
type Bone = {
  name: string;
  pivot?: [number, number, number];
  rotation?: [number, number, number];
  cubes?: Cube[];
  children?: Bone[];
};
type Geometry = {
  texture_width?: number;
  texture_height?: number;
  bones: Bone[];
  display?: Record<string, {
    rotation?: [number, number, number];
    translation?: [number, number, number];
    scale?: [number, number, number];
  }>;
};
type Animation = { length: number; loop: boolean; bones: Record<string, Timeline> };
type Animations = { animations: Record<string, Animation> };

interface BonePreviewProps {
  store: AssetStore;
  geometryPath: string;
  animationsPath?: string;
  texturePath: string;
  action: string;
  playing: boolean;
  mode: "bone" | "texture" | "combined";
  context?: string;
  interactive?: boolean;
  displayTransform?: "game" | "editor";
  fitScale?: number;
  textureSlots?: Record<string, string>;
  textureAnimation?: { frames: string[]; frame_duration: number; trigger?: string; interval?: { min: number; max: number } };
  texturePlaying?: boolean;
  previewYaw?: number;
}

async function readJson<T>(store: AssetStore, path: string): Promise<T> {
  const value = await store.read(path);
  const text = typeof value === "string" ? value : await value.text();
  try {
    return JSON.parse(text) as T;
  } catch (error) {
    throw new Error(`JSON 无法解析：${path}（${error instanceof Error ? error.message : "未知错误"}）`);
  }
}

function defaultFaces(cube: Cube): Partial<Record<MinecraftDirection, Face>> {
  const [u, v] = cube.uv ?? [0, 0];
  const [width, height, depth] = cube.size;
  return {
    north: { uv: [u + depth, v + depth, u + depth + width, v + depth + height] },
    south: { uv: [u + depth + width + depth, v + depth, u + depth + width + depth + width, v + depth + height] },
    west: { uv: [u, v + depth, u + depth, v + depth + height] },
    east: { uv: [u + depth + width, v + depth, u + depth + width + depth, v + depth + height] },
    up: { uv: [u + depth, v, u + depth + width, v + depth] },
    down: { uv: [u + depth + width, v, u + depth + width + width, v + depth] }
  };
}

export function BonePreview({
  store,
  geometryPath,
  animationsPath,
  texturePath,
  action,
  playing,
  mode,
  context = "gui"
  , interactive = true
  , displayTransform = "game"
  , fitScale = 2.4
  , textureSlots = {}
  , textureAnimation
  , texturePlaying = true
  , previewYaw = 0
}: BonePreviewProps) {
  const host = useRef<HTMLDivElement>(null);
  const [error, setError] = useState("");
  const playingRef = useRef(playing);
  const texturePlayingRef = useRef(texturePlaying);

  useEffect(() => { playingRef.current = playing; }, [playing]);
  useEffect(() => { texturePlayingRef.current = texturePlaying; }, [texturePlaying]);

  useEffect(() => {
    if (!host.current || mode === "texture") return;
    const container = host.current;
    setError("");
    const scene = new THREE.Scene();
    scene.background = new THREE.Color("#111713");
    const camera = new THREE.PerspectiveCamera(35, 1, 0.01, 100);
    camera.position.set(1.7, 1.35, 2.5);
    camera.lookAt(0, 0.15, 0);
    const renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.outputColorSpace = THREE.SRGBColorSpace;
    renderer.setPixelRatio(Math.min(devicePixelRatio, 2));
    container.prepend(renderer.domElement);
    scene.add(new THREE.HemisphereLight(0xffffff, 0x26372b, 2.5));
    const light = new THREE.DirectionalLight(0xffe0a2, 2.2);
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

    const bones = new Map<string, THREE.Group>();
    const basePositions = new Map<string, THREE.Vector3>();
    const baseRotations = new Map<string, THREE.Euler>();
    const baseScales = new Map<string, THREE.Vector3>();
    const objectUrls: string[] = [];
    const animatedTextures = new Map<string, THREE.Texture>();
    const animatedMaterials: THREE.MeshLambertMaterial[] = [];
    let activeTextureFrame = -1;
    let textureLastTick = 0;
    let texturePlaybackTick = 0;
    let textureWaiting = textureAnimation?.trigger === "random_idle";
    let textureWaitTicks = textureAnimation?.interval
      ? textureAnimation.interval.min + Math.floor(Math.random() * (textureAnimation.interval.max - textureAnimation.interval.min + 1))
      : 0;
    let animation: Animation | undefined;
    let started = performance.now();
    let disposed = false;

    const makeFaceMesh = (
      cube: Cube,
      direction: MinecraftDirection,
      face: Face,
      texture: THREE.Texture,
      uvWidth: number,
      uvHeight: number,
      pivot: [number, number, number]
    ) => {
      const bounds = {
        minX: cube.origin[0], minY: cube.origin[1], minZ: cube.origin[2],
        maxX: cube.origin[0] + cube.size[0],
        maxY: cube.origin[1] + cube.size[1],
        maxZ: cube.origin[2] + cube.size[2]
      };
      const vertices = minecraftFaceVertices(bounds, direction);
      if (cube.mirror) vertices.reverse();
      const positions = vertices.flatMap(([x, y, z]) => [
        (x - pivot[0]) / 16,
        (y - pivot[1]) / 16,
        (z - pivot[2]) / 16
      ]);
      const uvs = [0, 1, 2, 3].flatMap((vertex) =>
        normalizedMinecraftUv(face.uv, face.rotation, vertex, uvWidth, uvHeight)
      );
      const geometry = new THREE.BufferGeometry();
      geometry.setAttribute("position", new THREE.Float32BufferAttribute(positions, 3));
      geometry.setAttribute("uv", new THREE.Float32BufferAttribute(uvs, 2));
      geometry.setIndex([0, 1, 2, 0, 2, 3]);
      geometry.computeVertexNormals();
      return new THREE.Mesh(geometry, new THREE.MeshLambertMaterial({
        map: texture,
        transparent: true,
        alphaTest: 0.01,
        side: THREE.DoubleSide
      }));
    };

    const build = (bone: Bone, parent: THREE.Group, texture: THREE.Texture, textureWidth: number, textureHeight: number) => {
      const pivot = bone.pivot ?? [0, 0, 0];
      const parentPivot = (parent.userData.pivot ?? [0, 0, 0]) as [number, number, number];
      const group = new THREE.Group();
      group.name = bone.name;
      group.position.set(
        (pivot[0] - parentPivot[0]) / 16,
        (pivot[1] - parentPivot[1]) / 16,
        (pivot[2] - parentPivot[2]) / 16
      );
      const rotation = bone.rotation ?? [0, 0, 0];
      group.rotation.set(...rotation.map(THREE.MathUtils.degToRad) as [number, number, number]);
      group.userData.pivot = pivot;
      parent.add(group);
      bones.set(bone.name, group);
      basePositions.set(bone.name, group.position.clone());
      baseRotations.set(bone.name, group.rotation.clone());
      baseScales.set(bone.name, group.scale.clone());
      for (const cube of bone.cubes ?? []) {
        const hasExplicitFaces = Boolean(cube.faces && Object.keys(cube.faces).length);
        const faces = hasExplicitFaces ? cube.faces! : defaultFaces(cube);
        const [uvWidth, uvHeight] = boneUvSize(hasExplicitFaces, textureWidth, textureHeight);
        for (const direction of MINECRAFT_DIRECTIONS) {
          const face = faces[direction];
          if (face) group.add(makeFaceMesh(cube, direction, face, texture, uvWidth, uvHeight, pivot));
        }
      }
      for (const child of bone.children ?? []) build(child, group, texture, textureWidth, textureHeight);
    };

    const controls = interactive ? installViewportControls(camera, renderer.domElement, modelRoot, new THREE.Vector3(0, 0.15, 0)) : null;
    const load = async () => {
      const geometry = await readJson<Geometry>(store, geometryPath);
      if (!geometry.bones?.length) throw new Error(`骨骼模型没有 bones：${geometryPath}`);
      let animations: Animations = { animations: {} };
      if (animationsPath) {
        try {
          animations = await readJson<Animations>(store, animationsPath);
        } catch (reason) {
          throw new Error(`动作文件加载失败：${animationsPath}（${reason instanceof Error ? reason.message : "未知错误"}）`);
        }
      }
      const loadTexture = async (path: string) => {
        const value = isMinecraftTextureResource(path)
          ? await minecraftRemoteAssets.resolve(path)
          : await store.read(path);
        if (!value) throw new Error(`无法下载 Minecraft 纹理：${path}`);
        const url = typeof value === "string" ? value : URL.createObjectURL(value);
        if (typeof value !== "string") objectUrls.push(url);
        const loaded = await new THREE.TextureLoader().loadAsync(url);
        loaded.flipY = false;
        loaded.magFilter = THREE.NearestFilter;
        loaded.minFilter = THREE.NearestFilter;
        loaded.colorSpace = THREE.SRGBColorSpace;
        return loaded;
      };
      const texture = await loadTexture(texturePath);
      if (textureAnimation) {
        for (const slot of new Set(textureAnimation.frames)) {
          const path = textureSlots[slot];
          if (path) animatedTextures.set(slot, await loadTexture(path));
        }
      }
      if (disposed) {
        texture.dispose();
        return;
      }
      for (const bone of geometry.bones) {
        build(bone, modelRoot, texture, geometry.texture_width ?? 64, geometry.texture_height ?? 64);
      }
      modelRoot.traverse((object) => {
        if (object instanceof THREE.Mesh && object.material instanceof THREE.MeshLambertMaterial) animatedMaterials.push(object.material);
      });
      const display = displayTransform === "game" ? geometry.display?.[context] : undefined;
      if (display) {
        const rotation = display.rotation ?? [0, 0, 0];
        const translation = display.translation ?? [0, 0, 0];
        modelRoot.rotation.set(...rotation.map(THREE.MathUtils.degToRad) as [number, number, number]);
        modelRoot.position.add(new THREE.Vector3(
          translation[0] / 16,
          translation[1] / 16,
          translation[2] / 16
        ));
        modelRoot.scale.set(...(display.scale ?? [1, 1, 1]));
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
      const animationName = resolveAnimationName(action, action, animations.animations);
      animation = animationName ? animations.animations[animationName] : Object.values(animations.animations)[0];
      started = performance.now();
    };
    load().catch((reason) => {
      if (!disposed) setError(reason instanceof Error ? reason.message : "骨骼模型加载失败");
    });

    let frame = 0;
    const draw = (now: number) => {
      if (animation) {
        const tick = playingRef.current ? (now - started) / 50 : 0;
        const time = animation.loop ? tick % Math.max(1, animation.length) : Math.min(tick, animation.length);
        for (const [name, timeline] of Object.entries(animation.bones)) {
          const bone = bones.get(name);
          if (!bone) continue;
          bone.position.copy(basePositions.get(name)!);
          bone.rotation.copy(baseRotations.get(name)!);
          bone.scale.copy(baseScales.get(name)!);
          if (timeline.rotation) {
            const value = interpolateBoneFrames(timeline.rotation, time, [0, 0, 0]);
            bone.rotation.set(...value.map(THREE.MathUtils.degToRad) as [number, number, number]);
          }
          if (timeline.position) {
            const value = interpolateBoneFrames(timeline.position, time, [0, 0, 0]);
            bone.position.add(new THREE.Vector3(value[0] / 16, value[1] / 16, value[2] / 16));
          }
          if (timeline.scale) bone.scale.set(...interpolateBoneFrames(timeline.scale, time, [1, 1, 1]));
        }
      }
      if (textureAnimation && textureAnimation.frames.length && animatedMaterials.length && texturePlayingRef.current) {
        const textureTicks = (now - started) / 50;
        const duration = Math.max(1, textureAnimation.frame_duration);
        const elapsedTicks = Math.max(0, Math.floor(textureTicks) - Math.floor(textureLastTick));
        textureLastTick = textureTicks;
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
          texturePlaybackTick = textureTicks;
        }
        if (textureWaiting) { renderer.render(scene, camera); frame = requestAnimationFrame(draw); return; }
        const index = Math.floor(texturePlaybackTick / duration) % textureAnimation.frames.length;
        if (index !== activeTextureFrame) {
          const slot = textureAnimation.frames[index];
          const nextTexture = animatedTextures.get(slot);
          if (nextTexture) animatedMaterials.forEach((material) => { material.map = nextTexture; material.needsUpdate = true; });
          activeTextureFrame = index;
        }
      }
      renderer.render(scene, camera);
      frame = requestAnimationFrame(draw);
    };
    draw(performance.now());

    const reset = () => controls?.reset();
    if (controls) container.addEventListener("model-preview-reset", reset);
    const resize = () => {
      const width = Math.max(container.clientWidth, 1);
      const height = Math.max(container.clientHeight, 1);
      renderer.setSize(width, height);
      camera.aspect = width / height;
      camera.updateProjectionMatrix();
    };
    const observer = new ResizeObserver(resize);
    observer.observe(container);
    resize();

    return () => {
      disposed = true;
      cancelAnimationFrame(frame);
      observer.disconnect();
      if (controls) {
        container.removeEventListener("model-preview-reset", reset);
        controls.dispose();
      }
      scene.traverse((object) => {
        if (!(object instanceof THREE.Mesh)) return;
        object.geometry.dispose();
        const materials = Array.isArray(object.material) ? object.material : [object.material];
        materials.forEach((material) => {
          if (material instanceof THREE.MeshLambertMaterial) material.map?.dispose();
          material.dispose();
        });
      });
      objectUrls.forEach(URL.revokeObjectURL);
      renderer.dispose();
      renderer.domElement.remove();
    };
  }, [store, geometryPath, animationsPath, texturePath, action, mode, context, interactive, displayTransform, fitScale, JSON.stringify(textureSlots), JSON.stringify(textureAnimation)]);

  return (
    <div className="bone-preview model-viewport" ref={host}>
      {mode === "texture" && <span className="preview-message">纹理帧模式</span>}
      {interactive && error && <div className="preview-message error">{error}</div>}
      {mode !== "texture" && interactive && <button
          className="viewport-reset"
          type="button"
          onClick={() => host.current?.dispatchEvent(new Event("model-preview-reset"))}
        >
          <Icon name="reset" />
      </button>}
    </div>
  );
}
