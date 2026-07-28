import type { AssetStore } from "./lib/asset-store";
import type { FaceUv, MinecraftDirection, Vec3 } from "./minecraft-face";

export interface ItemModelFace {
  uv: FaceUv;
  rotation?: number;
  texture?: string;
  tintindex?: number;
  cullface?: MinecraftDirection;
}

export interface ItemModelElement {
  from: Vec3;
  to: Vec3;
  shade?: boolean;
  faces: Partial<Record<MinecraftDirection, ItemModelFace>>;
  rotation?: {
    angle?: number;
    axis?: "x" | "y" | "z";
    origin?: Vec3;
    rescale?: boolean;
  };
}

export interface ItemDisplayTransform {
  rotation?: Vec3;
  translation?: Vec3;
  scale?: Vec3;
}

export interface ParsedItemModel {
  parent?: string;
  ambientocclusion?: boolean;
  textures: Record<string, string>;
  elements: ItemModelElement[];
  display: Record<string, ItemDisplayTransform>;
}

type RawItemModel = Partial<ParsedItemModel>;

function directory(path: string): string {
  const slash = path.lastIndexOf("/");
  return slash < 0 ? "" : path.slice(0, slash);
}

function normalizePath(path: string): string {
  const parts: string[] = [];
  for (const part of path.replaceAll("\\", "/").split("/")) {
    if (!part || part === ".") continue;
    if (part === "..") parts.pop();
    else parts.push(part);
  }
  return parts.join("/");
}

function parentCandidates(parent: string, modelPath: string): string[] {
  const candidates: string[] = [];
  if (parent.includes(":")) {
    const [namespace, resource] = parent.split(":", 2);
    candidates.push(`assets/${namespace}/models/${resource}.json`);
    candidates.push(`models/${resource}.json`);
  } else {
    const suffix = parent.endsWith(".json") ? parent : `${parent}.json`;
    candidates.push(normalizePath(`${directory(modelPath)}/${suffix}`));
    candidates.push(normalizePath(`models/${suffix}`));
  }
  return [...new Set(candidates)];
}

async function readJson(store: AssetStore, path: string): Promise<RawItemModel> {
  const value = await store.read(path);
  const text = typeof value === "string" ? value : await value.text();
  try {
    return JSON.parse(text) as RawItemModel;
  } catch (error) {
    throw new Error(`模型 JSON 无法解析：${path}（${error instanceof Error ? error.message : "未知错误"}）`);
  }
}

async function findParent(store: AssetStore, parent: string, modelPath: string): Promise<string | null> {
  for (const candidate of parentCandidates(parent, modelPath)) {
    if (store.list().length && !store.has(candidate)) continue;
    try {
      await store.read(candidate);
      return candidate;
    } catch {
      // Continue through compatible local and resource-pack layouts.
    }
  }
  return null;
}

export async function parseItemModel(
  store: AssetStore,
  modelPath: string,
  visited = new Set<string>()
): Promise<ParsedItemModel> {
  if (visited.has(modelPath)) throw new Error(`模型父级存在循环引用：${modelPath}`);
  visited.add(modelPath);
  const raw = await readJson(store, modelPath);

  let inherited: ParsedItemModel = { textures: {}, elements: [], display: {} };
  if (raw.parent) {
    const parentPath = await findParent(store, raw.parent, modelPath);
    if (!parentPath) throw new Error(`找不到父模型：${raw.parent}（由 ${modelPath} 引用）`);
    inherited = await parseItemModel(store, parentPath, visited);
  }

  return {
    parent: raw.parent,
    ambientocclusion: raw.ambientocclusion ?? inherited.ambientocclusion,
    textures: { ...inherited.textures, ...(raw.textures ?? {}) },
    elements: raw.elements ?? inherited.elements,
    display: { ...inherited.display, ...(raw.display ?? {}) }
  };
}
