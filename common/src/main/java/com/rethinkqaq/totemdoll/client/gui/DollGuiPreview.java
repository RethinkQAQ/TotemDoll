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

import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import com.rethinkqaq.totemdoll.utils.DollGuiGraphics;
import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import com.rethinkqaq.totemdoll.utils.DollNativeItemUtil;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import net.minecraft.world.item.ItemStack;
//? >= 1.21.6 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.render.TextureSetup;
*///?}

/** Renders a TotemDoll preview using the best GUI path for the active version. */
public final class DollGuiPreview {
    private static final DollResourceId TOTEM_TEXTURE =
            DollResourceId.ofVanilla("textures/item/totem_of_undying.png");

    private DollGuiPreview() {
    }

    public static void render(DollGuiGraphics graphics, DollStyle style, int x, int y, int width, int height, float modelScale) {
        render(graphics, style, x, y, width, height, modelScale, x, y, width, height);
    }

    public static void render(DollGuiGraphics graphics, DollStyle style, int x, int y, int width, int height,
                              float modelScale, int clipX, int clipY, int clipWidth, int clipHeight) {
        if (!DollBoneModels.contains(style.id())) {
            ItemStack totem = DollNativeItemUtil.createTotemStack();
            renderItemPreview(graphics, x, y, width, height, modelScale,
                    () -> DollPreviewContext.renderNative(
                            () -> renderNativeTotem(graphics, totem)));
            return;
        }

        //? >= 1.21.6 {
        /*graphics.submitPreview(
                new DollGuiPreviewRenderState(style, x, y, width, height, modelScale,
                        clipX, clipY, clipWidth, clipHeight)
        );
        *///?} else {
        renderItemPreview(graphics, x, y, width, height, modelScale,
                () -> DollPreviewContext.renderAs(style,
                        () -> graphics.renderItem(DollNativeItemUtil.createTotemStack(), 0, 0)));
        //?}
    }

    private static void renderNativeTotem(DollGuiGraphics graphics, ItemStack totem) {
        if (totem.isEmpty()) {
            graphics.blitTexture(DollMinecraftResourceUtil.nativeId(TOTEM_TEXTURE),
                    0, 0, 0, 0, 16, 16, 16, 16);
        } else {
            graphics.renderItem(totem, 0, 0);
        }
    }

    //? >= 1.21.11 {
    /*public static TextureSetup singleTexture(GpuTextureView view) {
        return TextureSetup.singleTexture(view,
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
    }
    *///?} else if >= 1.21.6 {
    /*public static TextureSetup singleTexture(GpuTextureView view) {
        return TextureSetup.singleTexture(view);
    }
    *///?}

    private static void renderItemPreview(
            DollGuiGraphics graphics, int x, int y, int width, int height, float modelScale, Runnable renderCall
    ) {
        float scale = modelScale / 16.0F;
        graphics.pushPose();
        try {
            graphics.translate(
                    x + (width - modelScale) / 2.0F,
                    y + (height - modelScale) / 2.0F,
                    0.0F);
            graphics.scale(scale, scale, 1.0F);
            renderCall.run();
        } finally {
            graphics.popPose();
        }
    }
}
