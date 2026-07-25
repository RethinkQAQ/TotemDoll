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
 *
 * Totem Doll is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Totem Doll. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollAnimationDefinition;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import com.rethinkqaq.totemdoll.doll.bone.BonePose;
import com.rethinkqaq.totemdoll.doll.bone.DollBone;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneActionManager;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModel;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import com.rethinkqaq.totemdoll.doll.bone.DollCube;
import com.rethinkqaq.totemdoll.doll.bone.DollDisplayTransform;
import com.rethinkqaq.totemdoll.doll.bone.DollFace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

public final class DollBoneRenderer {
    private static final Map<net.minecraft.resources.ResourceLocation, RuntimeModel> CACHE = new ConcurrentHashMap<>();

    public static boolean render(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
                                 BakedModel referenceModel, float partialTick) {
        DollBoneModel model = DollBoneModels.get(style.id());
        if (model == null) return false;
        RuntimeModel runtime = CACHE.computeIfAbsent(style.id(), ignored -> build(model));
        applyPoses(style, runtime, partialTick);

        poseStack.pushPose();
        DollDisplayTransform display = model.display().get(context.getSerializedName());
        if (display == null) {
            referenceModel.getTransforms().getTransform(context).apply(leftHand, poseStack);
        } else {
            new ItemTransform(
                    new Vector3f(display.rotationX(), display.rotationY(), display.rotationZ()),
                    new Vector3f(display.translationX(), display.translationY(), display.translationZ()),
                    new Vector3f(display.scaleX(), display.scaleY(), display.scaleZ())
            ).apply(leftHand, poseStack);
        }
        // geometry.json uses the same 0..16, Y-up coordinate space as a
        // Minecraft Java item model. ModelPart is commonly used for entity
        // models together with a Y/Z flip, but applying that convention here
        // turns this item model upside down (head below body, limbs above it).
        // Match ItemRenderer's baked-model origin instead.
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        // Player skin overlays may contain partial alpha. The cutout render
        // type treats those pixels as opaque; translucent keeps the second
        // skin layer's full alpha channel.
        net.minecraft.resources.ResourceLocation texture = model.texture();
        if (DollAnimationManager.isTotemActivationActive(style)
                && style.textures().containsKey("activate")) {
            texture = style.textures().get("activate");
        } else if (style.hasDynamicTextures() && !style.animations().isEmpty()) {
            DollAnimationDefinition animation = style.animations().get(0);
            int frame = DollAnimationManager.currentFrame(style, animation.id());
            if (frame >= 0 && frame < animation.frames().size()) {
                texture = style.textures().getOrDefault(animation.frames().get(frame), texture);
            }
        }
        var consumer = buffers.getBuffer(RenderType.entityTranslucent(texture));
        for (RuntimePart root : runtime.roots)
            renderPart(root, poseStack, consumer, light, overlay);
        poseStack.popPose();
        return true;
    }

    public static void clear() { CACHE.clear(); }

    private static RuntimeModel build(DollBoneModel model) {
        Map<String, ModelPart> named = new HashMap<>();
        Map<String, BasePose> bases = new HashMap<>();
        List<RuntimePart> roots = new ArrayList<>();
        for (DollBone bone : model.roots()) {
            roots.add(buildPart(bone, null, model, named, bases));
        }
        return new RuntimeModel(List.copyOf(roots), named, bases);
    }

    private static RuntimePart buildPart(DollBone bone, DollBone parent, DollBoneModel model,
                                         Map<String, ModelPart> named, Map<String, BasePose> bases) {
        List<ModelPart.Cube> legacyCubes = new ArrayList<>();
        List<DollCube> faceCubes = new ArrayList<>();
        for (var cube : bone.cubes()) {
            if (cube.faces().isEmpty()) {
                legacyCubes.add(new ModelPart.Cube(cube.u(), cube.v(),
                        cube.x() - bone.pivotX(), cube.y() - bone.pivotY(), cube.z() - bone.pivotZ(),
                        cube.width(), cube.height(), cube.depth(), 0, 0, 0, cube.mirror(),
                        model.textureWidth(), model.textureHeight(), EnumSet.allOf(Direction.class)));
            } else {
                faceCubes.add(cube);
            }
        }
        ModelPart part = new ModelPart(List.of(), Map.of());
        float parentX = parent == null ? 0 : parent.pivotX();
        float parentY = parent == null ? 0 : parent.pivotY();
        float parentZ = parent == null ? 0 : parent.pivotZ();
        part.setPos(bone.pivotX() - parentX, bone.pivotY() - parentY, bone.pivotZ() - parentZ);
        part.setRotation(radians(bone.rotationX()), radians(bone.rotationY()), radians(bone.rotationZ()));
        named.put(bone.name(), part);
        bases.put(bone.name(), new BasePose(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot));
        List<RuntimePart> children = new ArrayList<>();
        for (DollBone child : bone.children())
            children.add(buildPart(child, bone, model, named, bases));
        return new RuntimePart(part, bone, List.copyOf(legacyCubes), List.copyOf(faceCubes),
                List.copyOf(children));
    }

    private static void renderPart(RuntimePart runtime, PoseStack poseStack, VertexConsumer consumer,
                                   int light, int overlay) {
        poseStack.pushPose();
        runtime.transform.translateAndRotate(poseStack);
        for (ModelPart.Cube cube : runtime.legacyCubes)
            cube.compile(poseStack.last(), consumer, light, overlay, -1);
        for (DollCube cube : runtime.faceCubes)
            renderCube(cube, runtime.bone, poseStack, consumer, light, overlay);
        for (RuntimePart child : runtime.children)
            renderPart(child, poseStack, consumer, light, overlay);
        poseStack.popPose();
    }

    private static void renderCube(DollCube cube, DollBone bone, PoseStack poseStack,
                                   VertexConsumer consumer, int light, int overlay) {
        float minX = cube.x() - bone.pivotX();
        float minY = cube.y() - bone.pivotY();
        float minZ = cube.z() - bone.pivotZ();
        float maxX = minX + cube.width();
        float maxY = minY + cube.height();
        float maxZ = minZ + cube.depth();
        float[] shape = new float[6];
        shape[Direction.WEST.get3DDataValue()] = minX;
        shape[Direction.DOWN.get3DDataValue()] = minY;
        shape[Direction.NORTH.get3DDataValue()] = minZ;
        shape[Direction.EAST.get3DDataValue()] = maxX;
        shape[Direction.UP.get3DDataValue()] = maxY;
        shape[Direction.SOUTH.get3DDataValue()] = maxZ;

        for (Map.Entry<String, DollFace> entry : cube.faces().entrySet()) {
            Direction direction = Direction.byName(entry.getKey());
            if (direction == null) continue;
            DollFace face = entry.getValue();
            BlockFaceUV uv = new BlockFaceUV(
                    new float[]{face.u1(), face.v1(), face.u2(), face.v2()}, face.rotation());
            FaceInfo info = FaceInfo.fromFacing(direction);
            for (int vertex = 0; vertex < 4; vertex++) {
                FaceInfo.VertexInfo position = info.getVertexInfo(vertex);
                consumer.addVertex(poseStack.last(), shape[position.xFace] / 16F,
                                shape[position.yFace] / 16F, shape[position.zFace] / 16F)
                        .setColor(-1)
                        .setUv(uv.getU(vertex) / 16F, uv.getV(vertex) / 16F)
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(poseStack.last(), direction.getStepX(), direction.getStepY(), direction.getStepZ());
            }
        }
    }

    private static void applyPoses(DollStyle style, RuntimeModel runtime, float partialTick) {
        for (Map.Entry<String, ModelPart> entry : runtime.named.entrySet()) {
            BasePose base = runtime.bases.get(entry.getKey());
            BonePose pose = DollBoneActionManager.pose(style, entry.getKey(), partialTick);
            ModelPart part = entry.getValue();
            part.setPos(base.x + pose.x(), base.y + pose.y(), base.z + pose.z());
            part.setRotation(base.xRot + radians(pose.xRot()), base.yRot + radians(pose.yRot()),
                    base.zRot + radians(pose.zRot()));
            part.xScale = pose.xScale();
            part.yScale = pose.yScale();
            part.zScale = pose.zScale();
        }
    }

    private static float radians(float degrees) { return degrees * ((float) Math.PI / 180F); }
    private record RuntimeModel(List<RuntimePart> roots, Map<String, ModelPart> named,
                                Map<String, BasePose> bases) {}
    private record RuntimePart(ModelPart transform, DollBone bone, List<ModelPart.Cube> legacyCubes,
                               List<DollCube> faceCubes, List<RuntimePart> children) {}
    private record BasePose(float x, float y, float z, float xRot, float yRot, float zRot) {}
    private DollBoneRenderer() {}
}
