import { describe, expect, it } from "vitest";
import { MINECRAFT_DIRECTIONS, boneUvSize, minecraftBlockFaceUv, minecraftFaceVertices } from "./minecraft-face";

describe("Minecraft FaceInfo compatibility", () => {
  const bounds = { minX: 1, minY: 2, minZ: 3, maxX: 4, maxY: 5, maxZ: 6 };

  it("uses the exact vertex order for all six faces", () => {
    expect(MINECRAFT_DIRECTIONS.map((direction) => minecraftFaceVertices(bounds, direction))).toEqual([
      [[1,2,6],[1,2,3],[4,2,3],[4,2,6]],
      [[1,5,3],[1,5,6],[4,5,6],[4,5,3]],
      [[4,5,3],[4,2,3],[1,2,3],[1,5,3]],
      [[1,5,6],[1,2,6],[4,2,6],[4,5,6]],
      [[1,5,3],[1,2,3],[1,2,6],[1,5,6]],
      [[4,5,6],[4,2,6],[4,2,3],[4,5,3]]
    ]);
  });
});

describe("Minecraft BlockFaceUV compatibility", () => {
  const uv: [number, number, number, number] = [2, 3, 11, 13];

  it.each([
    [0, [[2,3],[2,13],[11,13],[11,3]]],
    [90, [[2,13],[11,13],[11,3],[2,3]]],
    [180, [[11,13],[11,3],[2,3],[2,13]]],
    [270, [[11,3],[2,3],[2,13],[11,13]]]
  ])("maps %i degree rotation", (rotation, expected) => {
    expect([0,1,2,3].map((vertex) => minecraftBlockFaceUv(uv, rotation, vertex))).toEqual(expected);
  });

  it("preserves reversed and out-of-range coordinates", () => {
    expect(minecraftBlockFaceUv([16, -2, 0, 20], 0, 0)).toEqual([16, -2]);
    expect(minecraftBlockFaceUv([16, -2, 0, 20], 0, 2)).toEqual([0, 20]);
  });
});

describe("bone UV coordinate spaces", () => {
  it("uses /16 for explicit faces like DollBoneRenderer", () => {
    expect(boneUvSize(true, 64, 64)).toEqual([16, 16]);
  });

  it("uses texture dimensions for legacy ModelPart cubes", () => {
    expect(boneUvSize(false, 64, 32)).toEqual([64, 32]);
  });
});
