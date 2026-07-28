export type MinecraftDirection = "down" | "up" | "north" | "south" | "west" | "east";
export type Vec3 = [number, number, number];
export type FaceUv = [number, number, number, number];

export interface BoxBounds {
  minX: number;
  minY: number;
  minZ: number;
  maxX: number;
  maxY: number;
  maxZ: number;
}

export const MINECRAFT_DIRECTIONS: MinecraftDirection[] = [
  "down", "up", "north", "south", "west", "east"
];

/**
 * Vertex order used by Minecraft's FaceInfo. UV vertex indices must be applied
 * to this exact order; changing the winding independently rotates most faces.
 */
export function minecraftFaceVertices(bounds: BoxBounds, direction: MinecraftDirection): Vec3[] {
  const { minX, minY, minZ, maxX, maxY, maxZ } = bounds;
  const vertices: Record<MinecraftDirection, Vec3[]> = {
    down: [
      [minX, minY, maxZ], [minX, minY, minZ],
      [maxX, minY, minZ], [maxX, minY, maxZ]
    ],
    up: [
      [minX, maxY, minZ], [minX, maxY, maxZ],
      [maxX, maxY, maxZ], [maxX, maxY, minZ]
    ],
    north: [
      [maxX, maxY, minZ], [maxX, minY, minZ],
      [minX, minY, minZ], [minX, maxY, minZ]
    ],
    south: [
      [minX, maxY, maxZ], [minX, minY, maxZ],
      [maxX, minY, maxZ], [maxX, maxY, maxZ]
    ],
    west: [
      [minX, maxY, minZ], [minX, minY, minZ],
      [minX, minY, maxZ], [minX, maxY, maxZ]
    ],
    east: [
      [maxX, maxY, maxZ], [maxX, minY, maxZ],
      [maxX, minY, minZ], [maxX, maxY, minZ]
    ]
  };
  return vertices[direction];
}

/** Exact equivalent of BlockFaceUV#getU/getV for a vertex index. */
export function minecraftBlockFaceUv(uv: FaceUv, rotation = 0, vertex = 0): [number, number] {
  const turns = Math.floor((((rotation % 360) + 360) % 360) / 90);
  const shifted = (vertex + turns) % 4;
  const u = shifted === 0 || shifted === 1 ? uv[0] : uv[2];
  const v = shifted === 0 || shifted === 3 ? uv[1] : uv[3];
  return [u, v];
}

export function normalizedMinecraftUv(
  uv: FaceUv,
  rotation: number | undefined,
  vertex: number,
  textureWidth: number,
  textureHeight: number
): [number, number] {
  const [u, v] = minecraftBlockFaceUv(uv, rotation, vertex);
  // flipY=false makes image row zero correspond to Minecraft V=0.
  return [u / textureWidth, v / textureHeight];
}

/**
 * DollBoneRenderer sends explicit face UVs directly through BlockFaceUV (/16).
 * Legacy box UVs are compiled by ModelPart.Cube using the declared texture size.
 */
export function boneUvSize(
  hasExplicitFaces: boolean,
  textureWidth: number,
  textureHeight: number
): [number, number] {
  return hasExplicitFaces ? [16, 16] : [textureWidth, textureHeight];
}
