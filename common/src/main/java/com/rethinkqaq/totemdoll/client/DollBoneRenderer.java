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
import com.rethinkqaq.totemdoll.doll.bone.DollDisplayContext;
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
                                 float partialTick, boolean previewLighting) {
        return render(style, context, leftHand, poseStack, buffers, light, overlay, partialTick,
                previewLighting, true);
    }

    public static boolean render(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, MultiBufferSource buffers, int light, int overlay,
                                 float partialTick, boolean previewLighting, boolean allow3dSkinLayer) {
        PreparedModel prepared = prepare(style, context, leftHand, poseStack, partialTick, allow3dSkinLayer);
        if (prepared == null) return false;
        var consumer = buffers.getBuffer(previewLighting
                ? DollRenderUtil.previewTranslucent(prepared.texture())
                : DollRenderUtil.entityTranslucent(prepared.texture()));
        for (RuntimePart root : prepared.runtime().roots())
            renderPart(root, poseStack, consumer, light, overlay, prepared.skinLayers(), previewLighting);
        poseStack.popPose();
        return true;
    }
    //?}

//? >= 1.21.10 {
    /*public static boolean submit(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, SubmitNodeCollector nodeCollector, int light,
                                 int overlay, int outlineColor, boolean previewLighting) {
        return submit(style, context, leftHand, poseStack, nodeCollector, light, overlay, outlineColor,
                previewLighting, true);
    }

    public static boolean submit(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, SubmitNodeCollector nodeCollector, int light,
                                 int overlay, int outlineColor, boolean previewLighting, boolean allow3dSkinLayer) {
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        PreparedModel prepared = prepare(style, context, leftHand, poseStack, partialTick, allow3dSkinLayer);
        if (prepared == null) return false;

        nodeCollector.submitCustomGeometry(
                poseStack,
                previewLighting
                        ? DollRenderUtil.previewTranslucent(prepared.texture())
                        : DollRenderUtil.entityTranslucent(prepared.texture()),
                (capturedPose, consumer) -> {
                    PoseStack capturedStack = new PoseStack();
                    capturedStack.last().pose().set(capturedPose.pose());
                    capturedStack.last().normal().set(capturedPose.normal());
                    for (RuntimePart root : prepared.runtime().roots()) {
                        renderPart(root, capturedStack, consumer, light, overlay,
                                prepared.skinLayers(), previewLighting);
                    }
                }
        );
        poseStack.popPose();
        return true;
    }

    *///?}

    private static String displayContext(ItemDisplayContext context) {
        return switch (context) {
            case GUI -> DollDisplayContext.GUI;
            case GROUND -> DollDisplayContext.GROUND;
            case FIXED -> DollDisplayContext.FIXED;
            case FIRST_PERSON_LEFT_HAND -> DollDisplayContext.FIRSTPERSON_LEFTHAND;
            case FIRST_PERSON_RIGHT_HAND -> DollDisplayContext.FIRSTPERSON_RIGHTHAND;
            case THIRD_PERSON_LEFT_HAND -> DollDisplayContext.THIRDPERSON_LEFTHAND;
            case THIRD_PERSON_RIGHT_HAND -> DollDisplayContext.THIRDPERSON_RIGHTHAND;
            case HEAD -> DollDisplayContext.HEAD;
            //? >= 1.21.10 {
            /*case ON_SHELF -> DollDisplayContext.ON_SHELF;
            *///?}
            default -> DollDisplayContext.FIXED;
        };
    }

    private static boolean firstPersonPerspective(ItemDisplayContext context) {
        return switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND,
                    THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
                    Minecraft.getInstance().options.getCameraType().isFirstPerson();
            default -> false;
        };
    }

    private static PreparedModel prepare(DollStyle style, ItemDisplayContext context, boolean leftHand,
                                         PoseStack poseStack, float partialTick, boolean allow3dSkinLayer) {
        RenderData data = resolve(style, DollBoneModels.get(style.id()));
        if (data == null) return null;
        DollBoneModel model = data.model();
        RuntimeModel runtime = data.runtime();
        if (runtime.animated()) applyPoses(style, runtime, partialTick);

        poseStack.pushPose();
        ItemTransform display = displayTransform(runtime, context);
        if (display != null) {
            applyDisplayTransform(display, leftHand, poseStack);
        }
        // The legacy item renderer applies the model-space origin offset even
        // when an ItemTransform is present. Keep that ordering for the old
        // item and GUI paths; the newer render-state path only needs the
        // fallback when no display transform exists.
        //? < 1.21.5 {
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        //?} else {
        /*if (display == null) {
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        }
        *///?}
        DollResourceId texture = resolveTexture(style, model);
        DollSkinLayerRenderer.SkinLayerPlan skinLayers = DollSkinLayerRenderer.resolve(
                style, model, texture, allow3dSkinLayer);
        return new PreparedModel(runtime, texture, skinLayers);
    }

    private static ItemTransform displayTransform(RuntimeModel runtime, ItemDisplayContext context) {
        return runtime.displayTransforms().get(new DisplayKey(context, firstPersonPerspective(context)));
    }

    public static void clear() {
        CACHE.clear();
        DollSkinLayerRenderer.clear();
    }

    public static void clearSkinLayerCache() { DollSkinLayerRenderer.clear(); }

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
        return new RuntimeModel(List.copyOf(roots), named, bases, !model.animations().isEmpty(),
                buildDisplayTransforms(model));
    }

    private static Map<DisplayKey, ItemTransform> buildDisplayTransforms(DollBoneModel model) {
        Map<DisplayKey, ItemTransform> result = new HashMap<>();
        for (boolean firstPerson : new boolean[]{false, true}) {
            Map<String, DollDisplayTransform> displays = model.displays().forFirstPerson(firstPerson);
            for (ItemDisplayContext context : ItemDisplayContext.values()) {
                DollDisplayTransform display = DollDisplayContext.resolve(displays, displayContext(context));
                if (display == null) continue;
                result.put(new DisplayKey(context, firstPerson), new ItemTransform(
                        new Vector3f(display.rotationX(), display.rotationY(), display.rotationZ()),
                        new Vector3f(display.translationX(), display.translationY(), display.translationZ()),
                        new Vector3f(display.scaleX(), display.scaleY(), display.scaleZ())
                ));
            }
        }
        return Map.copyOf(result);
    }

    private static RuntimePart buildPart(DollBone bone, DollBone parent, DollBoneModel model,
                                         Map<String, ModelPart> named, Map<String, BasePose> bases) {
        List<ModelPart.Cube> legacyCubes = new ArrayList<>();
        List<RuntimeFace> faces = new ArrayList<>();
        for (var cube : bone.cubes()) {
            if (cube.faces().isEmpty()) {
                legacyCubes.add(new ModelPart.Cube(cube.u(), cube.v(),
                        cube.x() - bone.pivotX(), cube.y() - bone.pivotY(), cube.z() - bone.pivotZ(),
                        cube.width(), cube.height(), cube.depth(), 0, 0, 0, cube.mirror(),
                        model.textureWidth(), model.textureHeight(), EnumSet.allOf(Direction.class)));
            } else {
                faces.add(buildFace(cube, bone));
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
        return new RuntimePart(part, bone, List.copyOf(legacyCubes), List.copyOf(faces),
                List.copyOf(children));
    }

    private static RuntimeFace buildFace(DollCube cube, DollBone bone) {
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

        List<FaceVertex> vertices = new ArrayList<>(cube.faces().size() * 4);
        for (Map.Entry<String, DollFace> entry : cube.faces().entrySet()) {
            Direction direction = Direction.byName(entry.getKey());
            if (direction == null) continue;
            FaceInfo info = FaceInfo.fromFacing(direction);
            DollFace face = entry.getValue();
            for (int vertex = 0; vertex < 4; vertex++) {
                FaceInfo.VertexInfo position = info.getVertexInfo(vertex);
                float[] uv = UvUtil.vertexUv(face, vertex);
                vertices.add(new FaceVertex(
                        DollFaceUtil.x(position, shape, minX, minY, minZ, maxX, maxY, maxZ) / 16F,
                        DollFaceUtil.y(position, shape, minX, minY, minZ, maxX, maxY, maxZ) / 16F,
                        DollFaceUtil.z(position, shape, minX, minY, minZ, maxX, maxY, maxZ) / 16F,
                        uv[0] / 16F, uv[1] / 16F,
                        direction.getStepX(), direction.getStepY(), direction.getStepZ()
                ));
            }
        }
        return new RuntimeFace(cube, List.copyOf(vertices));
    }

    private static void renderPart(RuntimePart runtime, PoseStack poseStack, VertexConsumer consumer,
                                   int light, int overlay, DollSkinLayerRenderer.SkinLayerPlan skinLayers,
                                   boolean previewLighting) {
        poseStack.pushPose();
        runtime.transform.translateAndRotate(poseStack);
        for (ModelPart.Cube cube : runtime.legacyCubes)
            cube.compile(poseStack.last(), consumer, light, overlay, -1);
        for (RuntimeFace face : runtime.faces)
            if (!DollSkinLayerRenderer.isOverlayCube(skinLayers, runtime.bone.name(), face.cube()))
                renderFace(face, poseStack, consumer, light, overlay, previewLighting);
        DollSkinLayerRenderer.render(skinLayers, runtime.bone.name(), poseStack, consumer, light, overlay, previewLighting);
        for (RuntimePart child : runtime.children)
            renderPart(child, poseStack, consumer, light, overlay, skinLayers, previewLighting);
        poseStack.popPose();
    }

    private static void renderFace(RuntimeFace face, PoseStack poseStack, VertexConsumer consumer,
                                   int light, int overlay, boolean previewLighting) {
        for (FaceVertex vertex : face.vertices()) {
            float normalX = vertex.normalX();
            float normalY = vertex.normalY();
            float normalZ = vertex.normalZ();
            //? >= 1.21.5 {
            /*if (previewLighting) {
                normalX = 0.0F;
                normalY = 1.0F;
                normalZ = 0.0F;
            }
            *///?}
            consumer.addVertex(poseStack.last(), vertex.x(), vertex.y(), vertex.z())
                        .setColor(-1)
                        .setUv(vertex.u(), vertex.v())
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(poseStack.last(), normalX, normalY, normalZ);
        }
    }

    private static void applyDisplayTransform(ItemTransform transform, boolean leftHand, PoseStack poseStack) {
        transform.apply(
                leftHand,
                //? >= 1.21.5 {
                /*poseStack.last()
                *///?} else {
                poseStack
                //?}
        );
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
                                Map<String, BasePose> bases, boolean animated,
                                Map<DisplayKey, ItemTransform> displayTransforms) {}
    private record RenderData(DollBoneModel model, RuntimeModel runtime) {}
    private record PreparedModel(RuntimeModel runtime, DollResourceId texture,
                                 DollSkinLayerRenderer.SkinLayerPlan skinLayers) {}
    private record RuntimePart(ModelPart transform, DollBone bone, List<ModelPart.Cube> legacyCubes,
                               List<RuntimeFace> faces, List<RuntimePart> children) {}
    private record RuntimeFace(DollCube cube, List<FaceVertex> vertices) {}
    private record FaceVertex(float x, float y, float z, float u, float v,
                              float normalX, float normalY, float normalZ) {}
    private record DisplayKey(ItemDisplayContext context, boolean firstPerson) {}
    private record BasePose(float x, float y, float z, float xRot, float yRot, float zRot) {}
    private DollBoneRenderer() {}
}
