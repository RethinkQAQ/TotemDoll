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
import com.rethinkqaq.totemdoll.utils.UvUtil;
import com.rethinkqaq.totemdoll.utils.DollRenderUtil;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import com.rethinkqaq.totemdoll.utils.DollFaceUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.Minecraft;
//? < 26.2 {
import net.minecraft.client.renderer.MultiBufferSource;
//?}
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

//? >= 1.21.11 {
/*import net.minecraft.client.renderer.rendertype.RenderType;
*///?} else {
import net.minecraft.client.renderer.RenderType;
 //?}
import net.minecraft.client.renderer.FaceInfo;
//? >= 1.21.10 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
*///?}
//? >= 26.1.2 {
/*import net.minecraft.client.resources.model.cuboid.ItemTransform;
*///?} else {
import net.minecraft.client.renderer.block.model.ItemTransform;
//?}

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

public final class DollBoneRenderer {
    private static final Map<DollResourceId, RuntimeModel> CACHE = new ConcurrentHashMap<>();

    //? < 26.2 {
    public static boolean render(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
                                 float partialTick) {
        DollBoneModel model = DollBoneModels.get(style.id());
        RenderData data = resolve(style, model);
        if (data == null) return false;
        model = data.model();
        RuntimeModel runtime = data.runtime();
        applyPoses(style, runtime, partialTick);

        poseStack.pushPose();
        DollDisplayTransform display = model.display().get(displayContext(context));
        if (display == null) display = model.display().get("fixed");
        if (display != null) {
            new ItemTransform(
                    new Vector3f(display.rotationX(), display.rotationY(), display.rotationZ()),
                    new Vector3f(display.translationX(), display.translationY(), display.translationZ()),
                    new Vector3f(display.scaleX(), display.scaleY(), display.scaleZ())
            ).apply(
                    leftHand,
                    //? >= 1.21.5 {
                    /*poseStack.last()
                    *///?} else {
                    poseStack
                    //?}
            );
        }
        //? >= 1.21.5 {
        /*if (display == null) {
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        }
        *///?} else {
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        //?}
        DollResourceId texture = resolveTexture(style, model);
        var consumer = buffers.getBuffer(DollRenderUtil.entityTranslucent(texture));
        for (RuntimePart root : runtime.roots)
            renderPart(root, poseStack, consumer, light, overlay);
        poseStack.popPose();
        return true;
    }
    //?}

//? >= 1.21.10 {
    /*public static boolean submit(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, SubmitNodeCollector nodeCollector, int light,
                                 int overlay, int outlineColor) {
        RenderData data = resolve(style, DollBoneModels.get(style.id()));
        if (data == null) return false;
        DollBoneModel model = data.model();
        RuntimeModel runtime = data.runtime();
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        applyPoses(style, runtime, partialTick);

        poseStack.pushPose();
        DollDisplayTransform display = model.display().get(displayContext(context));
        if (display == null) display = model.display().get("fixed");
        if (display != null) {
            new ItemTransform(
                    new Vector3f(display.rotationX(), display.rotationY(), display.rotationZ()),
                    new Vector3f(display.translationX(), display.translationY(), display.translationZ()),
                    new Vector3f(display.scaleX(), display.scaleY(), display.scaleZ())
            ).apply(leftHand, poseStack.last());
        } else {
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        }

        DollResourceId texture = resolveTexture(style, model);

        DollResourceId finalTexture = texture;
        nodeCollector.submitCustomGeometry(
                poseStack,
                DollRenderUtil.entityTranslucent(finalTexture),
                (capturedPose, consumer) -> {
                    PoseStack capturedStack = new PoseStack();
                    capturedStack.last().pose().set(capturedPose.pose());
                    capturedStack.last().normal().set(capturedPose.normal());
                    for (RuntimePart root : runtime.roots) {
                        renderPart(root, capturedStack, consumer, light, overlay);
                    }
                }
        );
        poseStack.popPose();
        return true;
    }

    *///?}

    private static String displayContext(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> "gui";
            case GROUND -> "ground";
            case FIXED -> "fixed";
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> "firstperson";
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> "thirdperson";
            case HEAD -> "head";
            //? >= 1.21.10 {
            /*case ON_SHELF -> "on_shelf";
            *///?}
            default -> "fixed";
        };
    }

    public static void clear() { CACHE.clear(); }

    private static RenderData resolve(DollStyle style, DollBoneModel model) {
        if (model == null) return null;
        return new RenderData(model, CACHE.computeIfAbsent(style.id(), ignored -> build(model)));
    }

    private static DollResourceId resolveTexture(DollStyle style, DollBoneModel model) {
        DollResourceId texture = model.texture();
        if (DollAnimationManager.isTotemActivationActive(style)
                && style.textures().containsKey("activate")) {
            return style.textures().get("activate");
        }
        if (style.hasDynamicTextures()) {
            DollAnimationDefinition animation = DollAnimationManager.activeTextureAnimation(style);
            if (animation == null) return texture;
            int frame = animation.isLinked()
                    ? DollAnimationManager.currentLinkedFrame(style, animation.id())
                    : DollAnimationManager.currentFrame(style, animation.id());
            if (frame >= 0 && frame < animation.frames().size()) {
                return style.textures().getOrDefault(animation.frames().get(frame), texture);
            }
        }
        return texture;
    }

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
            FaceInfo info = FaceInfo.fromFacing(direction);
            for (int vertex = 0; vertex < 4; vertex++) {
                FaceInfo.VertexInfo position = info.getVertexInfo(vertex);
                float[] uv = UvUtil.vertexUv(face, vertex);
                consumer.addVertex(poseStack.last(), DollFaceUtil.x(position, shape, minX, minY, minZ, maxX, maxY, maxZ) / 16F,
                                DollFaceUtil.y(position, shape, minX, minY, minZ, maxX, maxY, maxZ) / 16F,
                                DollFaceUtil.z(position, shape, minX, minY, minZ, maxX, maxY, maxZ) / 16F)
                        .setColor(-1)
                        .setUv(uv[0] / 16F, uv[1] / 16F)
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
    private record RenderData(DollBoneModel model, RuntimeModel runtime) {}
    private record RuntimePart(ModelPart transform, DollBone bone, List<ModelPart.Cube> legacyCubes,
                               List<DollCube> faceCubes, List<RuntimePart> children) {}
    private record BasePose(float x, float y, float z, float xRot, float yRot, float zRot) {}
    private DollBoneRenderer() {}
}
