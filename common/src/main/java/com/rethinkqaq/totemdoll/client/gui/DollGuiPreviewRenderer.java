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
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public final class DollGuiPreviewRenderer extends PictureInPictureRenderer<DollGuiPreviewRenderState> {
    private static final int MAX_CACHED_PREVIEWS = 64;
    private static long invalidationGeneration;
    private final Map<DollGuiPreviewRenderState.PreviewKey, PreviewTarget> targets = new LinkedHashMap<>(16, 0.75F, true);
    private final CachedOrthoProjectionMatrixBuffer projection = new CachedOrthoProjectionMatrixBuffer(
            "totemdoll gui preview", -1000.0F, 1000.0F, true);
    private long renderedGeneration = -1;

    public DollGuiPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public void prepare(DollGuiPreviewRenderState state, GuiRenderState guiRenderState, int guiScale) {
        if (renderedGeneration != invalidationGeneration) {
            clearTargets();
            renderedGeneration = invalidationGeneration;
        }
        DollGuiPreviewRenderState.PreviewKey key = state.key(guiScale);
        PreviewTarget target = targets.get(key);
        if (target == null) {
            target = new PreviewTarget(key.width(), key.height());
            targets.put(key, target);
            trimTargets();
        }
        if (!target.ready || state.dynamic()) {
            renderTarget(state, target, guiScale);
            target.ready = true;
        }
        guiRenderState.submitBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA, TextureSetup.singleTexture(target.view), state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(), 0.0F, 1.0F, 1.0F, 0.0F,
                -1, state.scissorArea(), null));
    }

    private void renderTarget(DollGuiPreviewRenderState state, PreviewTarget target, int guiScale) {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.texture, 0, target.depth, 1.0);
        RenderSystem.outputColorTextureOverride = target.view;
        RenderSystem.outputDepthTextureOverride = target.depthView;
        RenderSystem.setProjectionMatrix(projection.getBuffer(target.width, target.height), ProjectionType.ORTHOGRAPHIC);
        PoseStack pose = new PoseStack();
        pose.translate(target.width / 2.0F, target.height / 2.0F, 0.0F);
        float scale = state.scale() * guiScale;
        pose.scale(scale, -scale, scale);
        DollBoneRenderer.render(state.style(), ItemDisplayContext.GUI, false, pose, bufferSource, 15728880,
                OverlayTexture.NO_OVERLAY, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        bufferSource.endBatch();
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
    }

    private void trimTargets() {
        while (targets.size() > MAX_CACHED_PREVIEWS) {
            Map.Entry<DollGuiPreviewRenderState.PreviewKey, PreviewTarget> eldest = targets.entrySet().iterator().next();
            eldest.getValue().close();
            targets.remove(eldest.getKey());
        }
    }

    private void clearTargets() {
        targets.values().forEach(PreviewTarget::close);
        targets.clear();
    }

    @Override
    public Class<DollGuiPreviewRenderState> getRenderStateClass() {
        return DollGuiPreviewRenderState.class;
    }

    public static void invalidateAll() {
        invalidationGeneration++;
    }

    @Override
    protected void renderToTexture(DollGuiPreviewRenderState state, PoseStack poseStack) {
    }

    @Override
    protected String getTextureLabel() {
        return "TotemDoll preview";
    }

    @Override
    public void close() {
        clearTargets();
        projection.close();
        super.close();
    }

    private static final class PreviewTarget implements AutoCloseable {
        final int width;
        final int height;
        final GpuTexture texture;
        final GpuTexture depth;
        final GpuTextureView view;
        final GpuTextureView depthView;
        boolean ready;

        PreviewTarget(int width, int height) {
            this.width = width;
            this.height = height;
            GpuDevice device = RenderSystem.getDevice();
            texture = device.createTexture("TotemDoll GUI preview", 12, TextureFormat.RGBA8, width, height, 1, 1);
            texture.setTextureFilter(FilterMode.NEAREST, false);
            view = device.createTextureView(texture);
            depth = device.createTexture("TotemDoll GUI preview depth", 8, TextureFormat.DEPTH32, width, height, 1, 1);
            depthView = device.createTextureView(depth);
        }

        @Override
        public void close() {
            texture.close();
            view.close();
            depth.close();
            depthView.close();
        }
    }
}
*///?} else {
public final class DollGuiPreviewRenderer {
    private DollGuiPreviewRenderer() {
    }

    public static void invalidateAll() {
    }
}
//?}
