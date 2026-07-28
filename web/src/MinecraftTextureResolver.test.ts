import { describe, expect, it } from "vitest";
import { memoryAssets } from "./lib/asset-store";
import { resolveModelTexture } from "./MinecraftTextureResolver";

describe("Minecraft texture variables", () => {
  it("resolves model variables through a style texture slot", () => {
    const store = memoryAssets(new Map([["textures/base.png", new Blob()]]));
    expect(resolveModelTexture("#1", { "1": "#base" }, { base: "textures/base.png" }, store)).toEqual({
      kind: "internal", path: "textures/base.png", source: "#1"
    });
  });

  it("does not substitute an external vanilla texture with base", () => {
    const store = memoryAssets(new Map([["textures/base.png", new Blob()]]));
    expect(resolveModelTexture("#1", { "1": "item/totem_of_undying" }, { base: "textures/base.png" }, store)).toEqual({
      kind: "external", resource: "item/totem_of_undying", source: "#1"
    });
  });

  it("reports cycles", () => {
    const store = memoryAssets(new Map());
    expect(resolveModelTexture("#a", { a: "#b", b: "#a" }, {}, store).kind).toBe("missing");
  });
});
