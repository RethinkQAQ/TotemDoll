import { describe, expect, it } from "vitest";
import { normalizeResourcePath } from "./minecraft-remote-assets";

describe("normalizeResourcePath", () => {
  it.each([
    ["minecraft:item/totem_of_undying", "assets/minecraft/textures/item/totem_of_undying.png"],
    ["item/totem_of_undying", "assets/minecraft/textures/item/totem_of_undying.png"],
    ["minecraft:textures/item/totem_of_undying.png", "assets/minecraft/textures/item/totem_of_undying.png"],
    ["assets/minecraft/textures/block/stone.png", "assets/minecraft/textures/block/stone.png"]
  ])("normalizes %s", (input, expected) => {
    expect(normalizeResourcePath(input)).toBe(expected);
  });

  it("rejects traversal and unsupported namespaces", () => {
    expect(() => normalizeResourcePath("item/../secret")).toThrow();
    expect(() => normalizeResourcePath("other:item/test")).toThrow();
  });
});
