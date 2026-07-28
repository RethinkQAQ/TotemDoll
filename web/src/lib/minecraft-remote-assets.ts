const VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
const MCASSET_BASE_URL = "https://assets.mcasset.cloud";
const CACHED_VERSION_KEY = "totemdoll.minecraft.latestRelease";

type VersionManifest = {
  latest?: {
    release?: string;
  };
};

export interface MinecraftRemoteAssetStatus {
  version: string;
  loading: boolean;
  cachedAssets: number;
  error: string;
}

function normalizeResourcePath(resourceId: string): string {
  let value = resourceId.trim().replaceAll("\\", "/");
  const colon = value.indexOf(":");
  if (colon >= 0) {
    const namespace = value.slice(0, colon);
    if (namespace && namespace !== "minecraft") {
      throw new Error(`不支持的外部资源命名空间：${namespace}`);
    }
    value = value.slice(colon + 1);
  }
  value = value.replace(/^assets\/minecraft\//, "");
  value = value.replace(/^textures\//, "");
  if (!/^(item|block|entity)\//.test(value)) {
    throw new Error(`不支持的 Minecraft 纹理路径：${resourceId}`);
  }
  if (value.startsWith("/") || value.split("/").includes("..")) {
    throw new Error(`非法 Minecraft 纹理路径：${resourceId}`);
  }
  if (!value.endsWith(".png")) value += ".png";
  return `assets/minecraft/textures/${value}`;
}

export function isMinecraftTextureResource(resourceId: string): boolean {
  const value = resourceId.trim().replaceAll("\\", "/");
  return value.startsWith("minecraft:")
    || /^(?:item|block|entity)\//.test(value)
    || value.startsWith("assets/minecraft/textures/");
}

export class MinecraftRemoteAssetProvider {
  private versionPromise: Promise<string> | null = null;
  private assets = new Map<string, Promise<Blob | null>>();
  private listeners = new Set<(status: MinecraftRemoteAssetStatus) => void>();
  private status: MinecraftRemoteAssetStatus = {
    version: "",
    loading: false,
    cachedAssets: 0,
    error: ""
  };

  subscribe(listener: (status: MinecraftRemoteAssetStatus) => void): () => void {
    this.listeners.add(listener);
    listener({ ...this.status });
    return () => this.listeners.delete(listener);
  }

  private emit(patch: Partial<MinecraftRemoteAssetStatus>) {
    this.status = { ...this.status, ...patch };
    for (const listener of this.listeners) listener({ ...this.status });
  }

  async getLatestRelease(): Promise<string> {
    if (!this.versionPromise) {
      this.versionPromise = (async () => {
        this.emit({ loading: true, error: "" });
        try {
          const response = await fetch(VERSION_MANIFEST_URL, { cache: "no-cache" });
          if (!response.ok) throw new Error(`Mojang 版本清单请求失败：HTTP ${response.status}`);
          const manifest = await response.json() as VersionManifest;
          const release = manifest.latest?.release;
          if (!release) throw new Error("Mojang 版本清单缺少 latest.release");
          localStorage.setItem(CACHED_VERSION_KEY, release);
          this.emit({ version: release, loading: false });
          return release;
        } catch (error) {
          const cached = localStorage.getItem(CACHED_VERSION_KEY);
          const message = error instanceof Error ? error.message : "无法获取 Minecraft 最新版本";
          this.emit({ version: cached ?? "", loading: false, error: message });
          if (cached) return cached;
          throw error;
        }
      })();
    }
    return this.versionPromise;
  }

  async resolve(resourceId: string): Promise<Blob | null> {
    const version = await this.getLatestRelease();
    const path = normalizeResourcePath(resourceId);
    const key = `${version}:${path}`;
    let pending = this.assets.get(key);
    if (!pending) {
      pending = (async () => {
        try {
          const response = await fetch(
            `${MCASSET_BASE_URL}/${encodeURIComponent(version)}/${path}`,
            { cache: "force-cache" }
          );
          if (!response.ok) throw new Error(`HTTP ${response.status}`);
          const type = response.headers.get("content-type") ?? "";
          if (!type.includes("image/png")) throw new Error(`返回类型不是 PNG：${type || "未知"}`);
          const blob = await response.blob();
          this.emit({ cachedAssets: this.status.cachedAssets + 1, error: "" });
          return blob;
        } catch (error) {
          const reason = error instanceof Error ? error.message : "未知错误";
          this.emit({ error: `${resourceId} 下载失败：${reason}` });
          return null;
        }
      })();
      this.assets.set(key, pending);
    }
    return pending;
  }

  clearVersionCache(version?: string) {
    if (!version) {
      this.assets.clear();
      this.versionPromise = null;
      this.emit({ version: "", cachedAssets: 0, error: "" });
      return;
    }
    for (const key of this.assets.keys()) {
      if (key.startsWith(`${version}:`)) this.assets.delete(key);
    }
    this.emit({ cachedAssets: this.assets.size });
  }
}

export const minecraftRemoteAssets = new MinecraftRemoteAssetProvider();
export { normalizeResourcePath };
