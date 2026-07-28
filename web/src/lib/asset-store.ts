export interface AssetStore {
  read(path: string): Promise<Blob | string>;
  has(path: string): boolean;
  list(): string[];
}

export function httpAssets(root: string): AssetStore {
  return {
    async read(path) { const response = await fetch(`${root}/${path}`); if (!response.ok) throw new Error(`Missing asset: ${path}`); return response.blob(); },
    has: () => true,
    list: () => []
  };
}

export function memoryAssets(files: Map<string, Blob>): AssetStore {
  return {
    read(path) { const value = files.get(path); if (!value) return Promise.reject(new Error(`Missing asset: ${path}`)); return Promise.resolve(value); },
    has: (path) => files.has(path),
    list: () => [...files.keys()]
  };
}

export async function assetUrl(store: AssetStore, path: string): Promise<string> {
  const value = await store.read(path);
  if (typeof value === "string") return value;
  return URL.createObjectURL(value);
}
