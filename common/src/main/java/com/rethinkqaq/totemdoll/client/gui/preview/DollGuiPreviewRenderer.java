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

package com.rethinkqaq.totemdoll.client.gui.preview;

//? >= 1.21.6 {
/*import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.client.gui.preview.DollGuiPreviewRenderState.PreviewKey;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
//? >= 1.21.6 && < 26.1.2 {
/^import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
^///?}
//? >= 26.1.2 && < 26.2 {
/^import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
^///?}
//? >= 26.2 {
/^import net.minecraft.client.gui.render.TextureSetup;
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
import net.minecraft.client.Minecraft;
//? >= 26.2 {
//?} else {
import net.minecraft.client.renderer.MultiBufferSource;
//?}

public final class DollGuiPreviewRenderer extends PictureInPictureRenderer<DollGuiPreviewRenderState> {
    //? >= 1.21.6 {
    /^private static DollGuiPreviewRenderer instance;
    private final DollGuiPreviewTargetCache targetCache = new DollGuiPreviewTargetCache();
    ^///?}
    //? >= 1.21.6 && < 26.1.2 {
/^private final CachedOrthoProjectionMatrixBuffer legacyProjectionBuffer = new CachedOrthoProjectionMatrixBuffer(
            "TotemDoll GUI preview", -1000.0F, 1000.0F, true
    );
    ^///?}
    //? >= 26.1.2 && < 26.2 {
/^private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("TotemDoll GUI preview");
    ^///?}
    //? >= 26.2 {
    /^private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("TotemDoll GUI preview");
    ^///?}
    //? >= 26.2 {
    /^public DollGuiPreviewRenderer() {
        super();
        instance = this;
    }
    ^///?} else {
    public DollGuiPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
        //? >= 1.21.6 && < 26.2 {
        /^instance = this;
        ^///?}
    }
    //?}

    @Override
    public Class<DollGuiPreviewRenderState> getRenderStateClass() {
        return DollGuiPreviewRenderState.class;
    }

    public static void invalidateAll() {
        //? >= 1.21.6 {
        /^if (instance != null) {
            DollGuiPreviewRenderer renderer = instance;
            Minecraft.getInstance().execute(renderer.targetCache::clear);
        }
        ^///?}
    }

    //? >= 1.21.6 && < 26.1.2 {
    /^@Override
    public void prepare(DollGuiPreviewRenderState state, GuiRenderState guiRenderState, int guiScale) {
        // Let Minecraft own the PIP target and GUI-layer composition. The former
        // custom target/blit path could cover ordinary RCUI elements with the
        // preview layer after the model had rendered.
        // Vanilla owns one shared PIP target. This screen submits several
        // previews per frame, so keep a target per preview key to prevent the
        // last rendered style from replacing every earlier blit.
        PreviewKey key = state.key(guiScale);
        DollGuiPreviewTarget target = targetCache.getOrCreate(key);

        if (target.needsRender(state.dynamic())) {
            RenderSystem.outputColorTextureOverride = target.colorView;
            RenderSystem.outputDepthTextureOverride = target.depthView;
            RenderSystem.backupProjectionMatrix();
            try {
                RenderSystem.getDevice().createCommandEncoder()
                        .clearColorAndDepthTextures(target.color, 0, target.depth, 1.0);
                RenderSystem.setProjectionMatrix(
                        legacyProjectionBuffer.getBuffer(key.width(), key.height()), ProjectionType.ORTHOGRAPHIC
                );

                PoseStack poseStack = new PoseStack();
                poseStack.translate(key.width() / 2.0F, getTranslateY(key.height(), guiScale), 0.0F);
                float scale = guiScale * state.scale();
                poseStack.scale(scale, scale, -scale);
                renderToTexture(state, poseStack);
                bufferSource.endBatch();
                target.markRendered();
            } finally {
                RenderSystem.restoreProjectionMatrix();
                RenderSystem.outputColorTextureOverride = null;
                RenderSystem.outputDepthTextureOverride = null;
            }
        }

        guiRenderState.submitBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                DollGuiPreview.singleTexture(target.colorView), state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(),
                0.0F, 1.0F, 1.0F, 0.0F, -1, state.scissorArea(), null
        ));
    }
    ^///?}

    //? >= 26.2 {
    /^@Override
    public void prepare(
            DollGuiPreviewRenderState state,
            GuiRenderState guiRenderState,
            FeatureRenderDispatcher featureRenderDispatcher,
            int guiScale
    ) {
        PreviewKey key = state.key(guiScale);
        DollGuiPreviewTarget target = targetCache.getOrCreate(key);

        RenderSystem.outputColorTextureOverride = target.colorView;
        RenderSystem.outputDepthTextureOverride = target.depthView;
        RenderSystem.backupProjectionMatrix();
        try {
            RenderSystem.getDevice().createCommandEncoder()
                    .clearColorAndDepthTextures(target.color, GuiRenderer.CLEAR_COLOR, target.depth, 0.0);
            projection.setupOrtho(-1000.0F, 1000.0F, key.width(), key.height(), true);
            RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC);

            PoseStack poseStack = new PoseStack();
            poseStack.translate(key.width() / 2.0F, key.height() / 2.0F, 0.0F);
            poseStack.scale(guiScale * state.scale(), -guiScale * state.scale(), guiScale * state.scale());
            SubmitNodeStorage storage = new SubmitNodeStorage();
            DollBoneRenderer.submit(state.style(), ItemDisplayContext.GUI, false, poseStack, storage,
                    15728880, OverlayTexture.NO_OVERLAY, 0, true);
            featureRenderDispatcher.renderAllFeatures(storage);
        } finally {
            RenderSystem.restoreProjectionMatrix();
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
        }

        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(target.colorView,
                        RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)), state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(),
                0.0F, 1.0F, 1.0F, 0.0F, -1, state.scissorArea(), null));
    }
    ^///?}

    //? >= 26.1.2 && < 26.2 {
/^@Override
    public void prepare(DollGuiPreviewRenderState state, GuiRenderState guiRenderState, int guiScale) {
        // TotemDoll supplies only renderToTexture; native PIP owns composition.
        // Vanilla owns one shared PIP target. This screen submits several
        // previews per frame, so keep a target per preview key to prevent the
        // last rendered style from replacing every earlier blit.
        PreviewKey key = state.key(guiScale);
        DollGuiPreviewTarget target = targetCache.getOrCreate(key);

        if (target.needsRender(state.dynamic())) {
            RenderSystem.outputColorTextureOverride = target.colorView;
            RenderSystem.outputDepthTextureOverride = target.depthView;
            RenderSystem.backupProjectionMatrix();
            try {
                RenderSystem.getDevice().createCommandEncoder()
                        .clearColorAndDepthTextures(target.color, 0, target.depth, 1.0);
                projection.setupOrtho(-1000.0F, 1000.0F, key.width(), key.height(), true);
                RenderSystem.setProjectionMatrix(
                        projectionBuffer.getBuffer(projection), ProjectionType.ORTHOGRAPHIC
                );

                PoseStack poseStack = new PoseStack();
                poseStack.translate(key.width() / 2.0F, getTranslateY(key.height(), guiScale), 0.0F);
                float scale = guiScale * state.scale();
                poseStack.scale(scale, scale, -scale);
                renderToTexture(state, poseStack);
                bufferSource.endBatch();
                target.markRendered();
            } finally {
                RenderSystem.restoreProjectionMatrix();
                RenderSystem.outputColorTextureOverride = null;
                RenderSystem.outputDepthTextureOverride = null;
            }
        }

        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                DollGuiPreview.singleTexture(target.colorView), state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(),
                0.0F, 1.0F, 1.0F, 0.0F, -1, state.scissorArea(), null
        ));
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
            DollPreviewContext.renderWithPreviewLighting(() -> DollBoneRenderer.submit(
                state.style(), ItemDisplayContext.GUI, false, poseStack, nodeCollector,
                15728880, OverlayTexture.NO_OVERLAY, 0, true
        ));
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
        DollPreviewContext.renderWithPreviewLighting(() -> DollBoneRenderer.render(
                state.style(), ItemDisplayContext.GUI, false, poseStack, bufferSource,
                15728880, OverlayTexture.NO_OVERLAY,
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), true
        ));
    }
    //?}

    @Override
    protected String getTextureLabel() {
        return "TotemDoll preview";
    }

    //? >= 1.21.6 && < 26.1.2 {
    /^@Override
    public void close() {
        targetCache.close();
        legacyProjectionBuffer.close();
        if (instance == this) {
            instance = null;
        }
        super.close();
    }

    ^///?}

    //? >= 26.2 {
    /^@Override
    public void close() {
        targetCache.close();
        projectionBuffer.close();
        if (instance == this) {
            instance = null;
        }
        super.close();
    }
    ^///?}

    //? >= 26.1.2 && < 26.2 {
/^@Override
    public void close() {
        targetCache.close();
        projectionBuffer.close();
        if (instance == this) {
            instance = null;
        }
        super.close();
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
