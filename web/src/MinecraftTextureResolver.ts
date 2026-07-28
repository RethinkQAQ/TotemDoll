import type { AssetStore } from "./lib/asset-store";

export type TextureResolution =
  | { kind: "internal"; path: string; source: string }
  | { kind: "external"; resource: string; source: string }
  | { kind: "missing"; source: string; reason: string };

function isExternalResource(value: string): boolean {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/i.test(value)
    || (!value.startsWith(".") && !value.includes("/") ? false : /^(?:item|block)\//.test(value));
}

export function resolveModelTexture(
  faceTexture: string | undefined,
  modelTextures: Record<string, string>,
  styleTextures: Record<string, string>,
  store: AssetStore
): TextureResolution {
  const source = faceTexture || "#base";
  const visited = new Set<string>();

  const resolve = (value: string): TextureResolution => {
    if (value.startsWith("#")) {
      const slot = value.slice(1);
      if (visited.has(slot)) return { kind: "missing", source, reason: `纹理变量循环引用：#${slot}` };
      visited.add(slot);
      if (styleTextures[slot]) return resolve(styleTextures[slot]);
      if (modelTextures[slot]) return resolve(modelTextures[slot]);
      return { kind: "missing", source, reason: `未定义纹理变量：#${slot}` };
    }

    if (styleTextures[value]) return resolve(styleTextures[value]);
    if (store.list().includes(value)) return { kind: "internal", path: value, source };
    if (isExternalResource(value)) return { kind: "external", resource: value, source };
    if (store.has(value)) return { kind: "internal", path: value, source };
    return { kind: "internal", path: value, source };
  };

  return resolve(source);
}
