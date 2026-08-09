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

package com.rethinkqaq.totemdoll.utils;

import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewAccess;
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderState;
import net.minecraft.client.gui.Font;
//? >= 26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
//? >= 1.21.11 {
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
//?} else if >= 1.21.6 {
/*import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
*///?} else if >= 1.21.3 {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
*///?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}

/** Version-independent wrapper for Minecraft's GUI extraction object. */
public final class DollGuiGraphics {
    //? >= 26.1.2 {
    private final GuiGraphicsExtractor graphics;
    //?} else {
    /*private final GuiGraphics graphics;
    *///?}

    private DollGuiGraphics(
            //? >= 26.1.2 {
            GuiGraphicsExtractor graphics
            //?} else {
            /*GuiGraphics graphics
            *///?}
    ) {
        this.graphics = graphics;
    }

    public static DollGuiGraphics wrap(
            //? >= 26.1.2 {
            GuiGraphicsExtractor graphics
            //?} else {
            /*GuiGraphics graphics
            *///?}
    ) {
        return new DollGuiGraphics(graphics);
    }

    public void fill(int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, bottom, color);
    }

    public void text(Font font, Component text, int x, int y, int color) {
        //? >= 26.1.2 {
        graphics.text(font, text, x, y, color);
        //?} else {
        /*graphics.drawString(font, text, x, y, color);
        *///?}
    }

    public void centeredText(Font font, Component text, int x, int y, int color) {
        //? >= 26.1.2 {
        graphics.centeredText(font, text, x, y, color);
        //?} else {
        /*graphics.drawCenteredString(font, text, x, y, color);
        *///?}
    }

    public void enableScissor(int left, int top, int right, int bottom) {
        graphics.enableScissor(left, top, right, bottom);
    }

    public void disableScissor() {
        graphics.disableScissor();
    }

    public void pushPose() {
        //? >= 1.21.6 {
        graphics.pose().pushMatrix();
        //?} else {
        /*graphics.pose().pushPose();
        *///?}
    }

    public void popPose() {
        //? >= 1.21.6 {
        graphics.pose().popMatrix();
        //?} else {
        /*graphics.pose().popPose();
        *///?}
    }

    public void translate(float x, float y, float z) {
        //? >= 1.21.6 {
        graphics.pose().translate(x, y);
        //?} else {
        /*graphics.pose().translate(x, y, z);
        *///?}
    }

    public void scale(float x, float y, float z) {
        //? >= 1.21.6 {
        graphics.pose().scale(x, y);
        //?} else {
        /*graphics.pose().scale(x, y, z);
        *///?}
    }

    public void renderItem(ItemStack stack, int x, int y) {
        //? >= 26.1.2 {
        graphics.item(stack, x, y);
        //?} else {
        /*graphics.renderItem(stack, x, y);
        *///?}
    }

    public void blitTexture(
            //? >= 1.21.11 {
            Identifier texture,
            //?} else {
            /*ResourceLocation texture,
            *///?}
            int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight
    ) {
        //? >= 1.21.6 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //?} else if >= 1.21.3 {
        /*graphics.blit(RenderType::guiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?} else {
        /*graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?}
    }

    public void submitPreview(DollGuiPreviewRenderState state) {
        //? >= 1.21.6 {
        ((DollGuiPreviewAccess) graphics).totemdoll$submitPreview(state);
        //?}
    }
}
