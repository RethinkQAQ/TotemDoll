/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 *
 * Totem Doll is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 */

package com.rethinkqaq.totemdoll.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.bone.DollBone;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModel;
import com.rethinkqaq.totemdoll.doll.bone.DollCube;
import com.rethinkqaq.totemdoll.doll.bone.DollFace;
import com.rethinkqaq.totemdoll.utils.DollFaceUtil;
import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import com.rethinkqaq.totemdoll.utils.UvUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.Resource;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Independently implemented, cached renderer for standard Minecraft outer skin pixels. */
public final class DollSkinLayerRenderer {
    /** Semi-transparent skin pixels stay on the original continuous outer layer. */
    private static final int VOXEL_ALPHA_THRESHOLD = 250;
    private static final Map<CacheKey, SkinLayerPlan> CACHE = new ConcurrentHashMap<>();
    private static final Set<String> SUPPORTED_BONES = Set.of(
            "head", "body", "left_arm", "right_arm", "left_leg", "right_leg"
    );

    public static SkinLayerPlan resolve(DollStyle style, DollBoneModel model, DollResourceId texture) {
        if (!TotemDollConfig.skinLayer3dEnabled() || model == null || texture == null) return null;
        CacheKey key = new CacheKey(style.id(), texture, TotemDollConfig.skinLayer3dThickness());
        return CACHE.computeIfAbsent(key, ignored -> build(model, texture));
    }

    public static void clear() { CACHE.clear(); }

    public static void render(SkinLayerPlan plan, String boneName, PoseStack stack,
                              VertexConsumer consumer, int light, int overlay) {
        if (plan == null) return;
        List<PixelQuad> quads = plan.quads.get(boneName);
        if (quads == null) return;
        for (PixelQuad quad : quads) renderQuad(quad, stack.last(), consumer, light, overlay);
    }

    public static boolean isOverlayCube(SkinLayerPlan plan, String boneName, DollCube cube) {
        return plan != null && plan.overlayCubes.getOrDefault(boneName, Set.of()).contains(cube);
    }

    private static SkinLayerPlan build(DollBoneModel model, DollResourceId texture) {
        NativeImage image = load(texture);
        if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
            if (image != null) image.close();
            return SkinLayerPlan.EMPTY;
        }
        Map<String, List<PixelQuad>> quads = new HashMap<>();
        Map<String, Set<DollCube>> overlayCubes = new HashMap<>();
        List<DollBone> bones = new ArrayList<>();
        for (DollBone root : model.roots()) collect(root, bones);
        Map<String, List<BoneCube>> partCubes = new HashMap<>();

        for (DollBone bone : bones) {
            String part = canonicalPart(bone.name());
            if (part == null) continue;
            for (DollCube cube : bone.cubes()) {
                if (!cube.faces().isEmpty()) {
                    partCubes.computeIfAbsent(part, ignored -> new ArrayList<>()).add(new BoneCube(bone, cube));
                }
            }
        }

        int matchedParts = 0;
        for (Map.Entry<String, List<BoneCube>> entry : partCubes.entrySet()) {
            BoneCube base = entry.getValue().stream()
                    .min(java.util.Comparator.comparingDouble(BoneCube::volume)).orElse(null);
            BoneCube outer = entry.getValue().stream()
                    .max(java.util.Comparator.comparingDouble(BoneCube::volume)).orElse(null);
            if (base == null || outer == null || base == outer) continue;
            matchedParts++;
            List<PixelQuad> partQuads = buildPart(image, base.bone(), outer.bone(), outer.cube());
            if (!partQuads.isEmpty()) {
                quads.put(base.bone().name(), List.copyOf(partQuads));
            }
        }
        image.close();
        if (matchedParts < SUPPORTED_BONES.size()) return SkinLayerPlan.EMPTY;
        return new SkinLayerPlan(quads, overlayCubes);
    }

    private static List<PixelQuad> buildPart(NativeImage image, DollBone renderBone, DollBone outerBone, DollCube outer) {
        float thickness = TotemDollConfig.skinLayer3dThickness();
        List<PixelQuad> result = new ArrayList<>();
        for (Map.Entry<String, DollFace> entry : outer.faces().entrySet()) {
            Direction direction = Direction.byName(entry.getKey());
            if (direction == null) continue;
            DollFace face = entry.getValue();
            float[][] outerCorners = faceCorners(direction, outerBone, outer, renderBone);
            int minU = (int) Math.floor(Math.min(face.u1(), face.u2()) * 4F);
            int maxU = (int) Math.ceil(Math.max(face.u1(), face.u2()) * 4F);
            int minV = (int) Math.floor(Math.min(face.v1(), face.v2()) * 4F);
            int maxV = (int) Math.ceil(Math.max(face.v1(), face.v2()) * 4F);
            for (int textureV = minV; textureV < maxV; textureV++) {
                for (int textureU = minU; textureU < maxU; textureU++) {
                    int alpha = alphaAt(image, textureU, textureV);
                    if (alpha < VOXEL_ALPHA_THRESHOLD) continue;
                    GridCell cell = GridCell.fromTexturePixel(face, textureU, textureV);
                    float[][] front = cell.points(outerCorners);
                    float[][] inner = offset(front, direction, -thickness);
                    float[] uvs = cell.uvs(face);
                    addVisibleWalls(result, image, direction, textureU, textureV, minU, maxU, minV, maxV, front, inner, uvs, alpha);
                }
            }
        }
        return result;
    }

    private static void addVisibleWalls(List<PixelQuad> result, NativeImage image, Direction direction, int u, int v,
                                        int minU, int maxU, int minV, int maxV, float[][] front, float[][] inner,
                                        float[] uvs, int alpha) {
        if (!opaque(image, u - 1, v, minU, maxU, minV, maxV)) addWall(result, front, inner, 0, 1, uvs, alpha);
        if (!opaque(image, u + 1, v, minU, maxU, minV, maxV)) addWall(result, front, inner, 3, 2, uvs, alpha);
        if (!opaque(image, u, v - 1, minU, maxU, minV, maxV)) addWall(result, front, inner, 0, 3, uvs, alpha);
        if (!opaque(image, u, v + 1, minU, maxU, minV, maxV)) addWall(result, front, inner, 1, 2, uvs, alpha);
    }

    private static boolean opaque(NativeImage image, int u, int v, int minU, int maxU, int minV, int maxV) {
        return u >= minU && u < maxU && v >= minV && v < maxV
                && alphaAt(image, u, v) >= VOXEL_ALPHA_THRESHOLD;
    }

    private static void addWall(List<PixelQuad> result, float[][] front, float[][] inner, int first, int second,
                                float[] uvs, int alpha) {
        float[][] wall = new float[][]{front[first], front[second], inner[second], inner[first]};
        float[] normal = normal(wall);
        float[] center = center(front);
        float[] edge = midpoint(front[first], front[second]);
        if (dot(normal, subtract(edge, center)) < 0) wall = new float[][]{front[second], front[first], inner[first], inner[second]};
        normal = normal(wall);
        result.add(new PixelQuad(wall, uvs, normal[0], normal[1], normal[2], alpha));
    }

    private static void renderQuad(PixelQuad quad, Pose pose, VertexConsumer consumer, int light, int overlay) {
        for (int vertex = 0; vertex < 4; vertex++) {
            float[] point = quad.points[vertex];
            consumer.addVertex(pose, point[0] / 16F, point[1] / 16F, point[2] / 16F)
                    // The texture already supplies per-pixel transparency. Applying the same
                    // alpha as a vertex colour would square it and create a visible pixel grid.
                    .setColor(255, 255, 255, 255)
                    .setUv(quad.uvs[vertex * 2] / 16F, quad.uvs[vertex * 2 + 1] / 16F)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(pose, quad.normalX, quad.normalY, quad.normalZ);
        }
    }

    private static float[][] faceCorners(Direction direction, DollBone outerBone, DollCube outer, DollBone renderBone) {
        float minX = outer.x() - outerBone.pivotX() + outerBone.pivotX() - renderBone.pivotX();
        float minY = outer.y() - outerBone.pivotY() + outerBone.pivotY() - renderBone.pivotY();
        float minZ = outer.z() - outerBone.pivotZ() + outerBone.pivotZ() - renderBone.pivotZ();
        float maxX = minX + outer.width(), maxY = minY + outer.height(), maxZ = minZ + outer.depth();
        float[] shape = new float[6];
        shape[Direction.WEST.get3DDataValue()] = minX; shape[Direction.DOWN.get3DDataValue()] = minY; shape[Direction.NORTH.get3DDataValue()] = minZ;
        shape[Direction.EAST.get3DDataValue()] = maxX; shape[Direction.UP.get3DDataValue()] = maxY; shape[Direction.SOUTH.get3DDataValue()] = maxZ;
        FaceInfo info = FaceInfo.fromFacing(direction);
        float[][] points = new float[4][];
        for (int vertex = 0; vertex < 4; vertex++) {
            FaceInfo.VertexInfo value = info.getVertexInfo(vertex);
            points[vertex] = new float[]{DollFaceUtil.x(value, shape, minX, minY, minZ, maxX, maxY, maxZ),
                    DollFaceUtil.y(value, shape, minX, minY, minZ, maxX, maxY, maxZ),
                    DollFaceUtil.z(value, shape, minX, minY, minZ, maxX, maxY, maxZ)};
        }
        return points;
    }

    private static float[][] offset(float[][] points, Direction direction, float distance) {
        float[][] result = new float[4][];
        for (int index = 0; index < 4; index++) result[index] = new float[]{points[index][0] + direction.getStepX() * distance,
                points[index][1] + direction.getStepY() * distance, points[index][2] + direction.getStepZ() * distance};
        return result;
    }

    private static float[] normal(float[][] points) {
        float[] first = subtract(points[1], points[0]), second = subtract(points[2], points[0]);
        float x = first[1] * second[2] - first[2] * second[1], y = first[2] * second[0] - first[0] * second[2], z = first[0] * second[1] - first[1] * second[0];
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        return length == 0 ? new float[]{0, 1, 0} : new float[]{x / length, y / length, z / length};
    }

    private static float[] subtract(float[] left, float[] right) { return new float[]{left[0] - right[0], left[1] - right[1], left[2] - right[2]}; }
    private static float[] midpoint(float[] left, float[] right) { return new float[]{(left[0] + right[0]) / 2F, (left[1] + right[1]) / 2F, (left[2] + right[2]) / 2F}; }
    private static float[] center(float[][] points) { return new float[]{(points[0][0] + points[1][0] + points[2][0] + points[3][0]) / 4F, (points[0][1] + points[1][1] + points[2][1] + points[3][1]) / 4F, (points[0][2] + points[1][2] + points[2][2] + points[3][2]) / 4F}; }
    private static float dot(float[] left, float[] right) { return left[0] * right[0] + left[1] * right[1] + left[2] * right[2]; }

    private static NativeImage load(DollResourceId texture) {
        var id = DollMinecraftResourceUtil.nativeId(texture);
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(id).orElse(null);
            if (resource == null) return null;
            try (InputStream input = resource.open()) {
                return NativeImage.read(input);
            }
        } catch (Exception exception) {
            return null;
        }
    }

    private static int alphaAt(NativeImage image, int x, int y) {
        //? >= 1.21.3 {
        /*return image.getPixel(x, y) >>> 24;
        *///?} else {
        return image.getPixelRGBA(x, y) >>> 24;
        //?}
    }

    private static void collect(DollBone bone, List<DollBone> result) {
        result.add(bone);
        for (DollBone child : bone.children()) collect(child, result);
    }

    private static String canonicalPart(String name) {
        String normalized = name.toLowerCase(java.util.Locale.ROOT);
        for (String part : SUPPORTED_BONES) {
            if (normalized.equals(part) || normalized.startsWith(part + "__")) return part;
        }
        return null;
    }

    private record CacheKey(DollResourceId style, DollResourceId texture, float thickness) {}
    private record BoneCube(DollBone bone, DollCube cube) {
        private double volume() { return cube.width() * cube.height() * cube.depth(); }
    }
    public record SkinLayerPlan(Map<String, List<PixelQuad>> quads,
                                Map<String, Set<DollCube>> overlayCubes) {
        private static final SkinLayerPlan EMPTY = new SkinLayerPlan(Map.of(), Map.of());
    }
    private record PixelQuad(float[][] points, float[] uvs, float normalX, float normalY, float normalZ, int alpha) {}

    /** Face-local rectangle in FaceInfo vertex order (0, 1, 2, 3). */
    private record GridCell(float s0, float t0, float s1, float t1) {
        private static GridCell fromTexturePixel(DollFace face, int textureU, int textureV) {
            float u0 = (textureU / 4F - face.u1()) / (face.u2() - face.u1());
            float u1 = ((textureU + 1) / 4F - face.u1()) / (face.u2() - face.u1());
            float v0 = (textureV / 4F - face.v1()) / (face.v2() - face.v1());
            float v1 = ((textureV + 1) / 4F - face.v1()) / (face.v2() - face.v1());
            return switch (Math.floorMod(face.rotation(), 360)) {
                case 90 -> new GridCell(1F - v1, u0, 1F - v0, u1);
                case 180 -> new GridCell(1F - u1, 1F - v1, 1F - u0, 1F - v0);
                case 270 -> new GridCell(v0, 1F - u1, v1, 1F - u0);
                default -> new GridCell(u0, v0, u1, v1);
            };
        }

        private float[] uvs(DollFace face) {
            return new float[]{uv(face, s0, t0, 0), uv(face, s0, t0, 1), uv(face, s0, t1, 0), uv(face, s0, t1, 1),
                    uv(face, s1, t1, 0), uv(face, s1, t1, 1), uv(face, s1, t0, 0), uv(face, s1, t0, 1)};
        }

        private float[][] points(float[][] corners) {
            return new float[][]{point(corners, s0, t0), point(corners, s0, t1), point(corners, s1, t1), point(corners, s1, t0)};
        }

        private static float[] point(float[][] corners, float s, float t) {
            return new float[]{component(corners, s, t, 0), component(corners, s, t, 1), component(corners, s, t, 2)};
        }

        private static float component(float[][] corners, float s, float t, int axis) {
            return corners[0][axis] * (1F - s) * (1F - t) + corners[1][axis] * (1F - s) * t
                    + corners[2][axis] * s * t + corners[3][axis] * s * (1F - t);
        }

        private static float uv(DollFace face, float s, float t, int component) {
            float[] a = vertexUv(face, 0), b = vertexUv(face, 1), c = vertexUv(face, 2), d = vertexUv(face, 3);
            return a[component] * (1F - s) * (1F - t) + b[component] * (1F - s) * t
                    + c[component] * s * t + d[component] * s * (1F - t);
        }

        private static float[] vertexUv(DollFace face, int vertex) {
            return switch (Math.floorMod(vertex + Math.floorMod(face.rotation(), 360) / 90, 4)) {
                case 0 -> new float[]{face.u1(), face.v1()};
                case 1 -> new float[]{face.u1(), face.v2()};
                case 2 -> new float[]{face.u2(), face.v2()};
                default -> new float[]{face.u2(), face.v1()};
            };
        }
    }

    private record FaceSpec(Direction direction, int u, int v, int width, int height) {}
    private static final class LayerSpec {
        private final int width;
        private final int height;
        private final int depth;
        private final List<FaceSpec> faces;

        private LayerSpec(int width, int height, int depth, int u, int v) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.faces = List.of(
                    new FaceSpec(Direction.DOWN, u + width, v, depth, width),
                    new FaceSpec(Direction.UP, u + width + depth, v, depth, width),
                    new FaceSpec(Direction.NORTH, u + depth, v + depth, width, height),
                    new FaceSpec(Direction.SOUTH, u + width + depth + depth, v + depth, width, height),
                    new FaceSpec(Direction.WEST, u, v + depth, depth, height),
                    new FaceSpec(Direction.EAST, u + width + depth, v + depth, depth, height)
            );
        }

        private static LayerSpec forPart(String part, DollCube base) {
            return switch (part) {
                case "head" -> new LayerSpec(8, 8, 8, 32, 0);
                case "body" -> new LayerSpec(8, 12, 4, 16, 32);
                case "right_arm" -> new LayerSpec(base.width() / base.height() < 0.28F ? 3 : 4, 12, 4, 40, 32);
                case "left_arm" -> new LayerSpec(base.width() / base.height() < 0.28F ? 3 : 4, 12, 4, 48, 48);
                case "right_leg" -> new LayerSpec(4, 12, 4, 0, 32);
                case "left_leg" -> new LayerSpec(4, 12, 4, 0, 48);
                default -> null;
            };
        }
    }

    private DollSkinLayerRenderer() {}
}
