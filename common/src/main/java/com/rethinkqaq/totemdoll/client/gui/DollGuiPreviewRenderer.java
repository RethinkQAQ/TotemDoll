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

package com.rethinkqaq.totemdoll.client.gui;

//? >= 1.21.6 {
/*import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderState.PreviewKey;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
//? >= 26.2 {
/^import com.mojang.blaze3d.GpuFormat;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
^///?} else {
import com.rethinkqaq.totemdoll.utils.Dummy;
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
//? >= 26.2 {
//?} else {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
//?}

import java.util.HashMap;
import java.util.Map;

public final class DollGuiPreviewRenderer extends PictureInPictureRenderer<DollGuiPreviewRenderState> {
    //? >= 26.2 {
    /^private final Map<PreviewKey, PreviewTarget> targets = new HashMap<>();
    private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("TotemDoll GUI preview");
    ^///?}
    //? >= 26.2 {
    /^public DollGuiPreviewRenderer() {
        super();
    }
    ^///?} else {
    public DollGuiPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }
    //?}

    @Override
    public Class<DollGuiPreviewRenderState> getRenderStateClass() {
        return DollGuiPreviewRenderState.class;
    }

    public static void invalidateAll() {
    }

    //? >= 26.2 {
    /^@Override
    public void prepare(
            DollGuiPreviewRenderState state,
            GuiRenderState guiRenderState,
            FeatureRenderDispatcher featureRenderDispatcher,
            int guiScale
    ) {
        PreviewKey key = state.key(guiScale);
        PreviewTarget target = targets.computeIfAbsent(key,
                ignored -> new PreviewTarget(key.width(), key.height()));

        GpuTexture color = target.color;
        GpuTextureView colorView = target.colorView;
        GpuTexture depth = target.depth;
        GpuTextureView depthView = target.depthView;
        RenderSystem.outputColorTextureOverride = colorView;
        RenderSystem.outputDepthTextureOverride = depthView;
        RenderSystem.getDevice().createCommandEncoder()
                .clearColorAndDepthTextures(color, GuiRenderer.CLEAR_COLOR, depth, 0.0);
        projection.setupOrtho(-1000.0F, 1000.0F, key.width(), key.height(), true);
        RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(key.width() / 2.0F, key.height() / 2.0F, 0.0F);
        poseStack.scale(guiScale * state.scale(), -guiScale * state.scale(), guiScale * state.scale());
        SubmitNodeStorage storage = new SubmitNodeStorage();
        DollBoneRenderer.submit(state.style(), ItemDisplayContext.GUI, false, poseStack, storage,
                15728880, OverlayTexture.NO_OVERLAY, 0);
        featureRenderDispatcher.renderAllFeatures(storage);
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(colorView,
                        RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)), state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(),
                0.0F, 1.0F, 1.0F, 0.0F, -1, state.scissorArea(), null));
    }
    ^///?}

    @Override
    //? >= 26.2 {
    /^protected void renderToTexture(
            DollGuiPreviewRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector
    ) {
        // PictureInPictureRenderer uses (x, y, -z), while the former GUI item
        // path used (x, -y, z). Keep the TotemDoll model coordinate system stable.
        poseStack.scale(1.0F, -1.0F, -1.0F);
        DollBoneRenderer.submit(
                state.style(), ItemDisplayContext.GUI, false, poseStack, nodeCollector,
                15728880, OverlayTexture.NO_OVERLAY, 0
        );
    }
    
    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    ^///?} else {
    protected void renderToTexture(DollGuiPreviewRenderState state, PoseStack poseStack) {
        poseStack.scale(1.0F, -1.0F, -1.0F);
        float verticalOffset = 0.75F * 37.6F / state.scale();
        poseStack.translate(0.0F, verticalOffset, 0.0F);
        DollBoneRenderer.render(
                state.style(), ItemDisplayContext.GUI, false, poseStack, bufferSource,
                15728880, OverlayTexture.NO_OVERLAY,
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
        );
    }
    //?}

    @Override
    protected String getTextureLabel() {
        return "TotemDoll preview";
    }

    //? >= 26.2 {
    /^@Override
    public void close() {
        targets.values().forEach(PreviewTarget::close);
        targets.clear();
        projectionBuffer.close();
        super.close();
    }

    private static final class PreviewTarget implements AutoCloseable {
        private final GpuTexture color;
        private final GpuTextureView colorView;
        private final GpuTexture depth;
        private final GpuTextureView depthView;

        private PreviewTarget(int width, int height) {
            var device = RenderSystem.getDevice();
            color = device.createTexture(() -> "TotemDoll GUI preview", 13,
                    GpuFormat.RGBA8_UNORM, width, height, 1, 1);
            colorView = device.createTextureView(color);
            depth = device.createTexture(() -> "TotemDoll GUI preview depth", 9,
                    GpuFormat.D32_FLOAT, width, height, 1, 1);
            depthView = device.createTextureView(depth);
        }

        @Override
        public void close() {
            colorView.close();
            color.close();
            depthView.close();
            depth.close();
        }
    }
    ^///?}
}
*///?} else {
public final class DollGuiPreviewRenderer {
    private DollGuiPreviewRenderer() {
    }

    public static void invalidateAll() {
    }
}
//?}
