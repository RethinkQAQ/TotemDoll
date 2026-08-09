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

//? >= 26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Predicate;

final class DollScreenRender {

    static void renderChildren(
            Screen screen,
            //? >= 26.1.2 {
            /*GuiGraphicsExtractor graphics,
            *///?} else {
            GuiGraphics graphics,
            //?}
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderChildren(screen, graphics, mouseX, mouseY, partialTick, child -> true);
    }

    static void renderChildren(
            Screen screen,
            //? >= 26.1.2 {
            /*GuiGraphicsExtractor graphics,
            *///?} else {
            GuiGraphics graphics,
            //?}
            int mouseX,
            int mouseY,
            float partialTick,
            Predicate<GuiEventListener> filter
    ) {
        for (var child : screen.children()) {
            if (filter.test(child) && child instanceof Renderable renderable) {
                //? >= 26.1.2 {
                /*renderable.extractRenderState(graphics, mouseX, mouseY, partialTick);
                *///?} else {
                renderable.render(graphics, mouseX, mouseY, partialTick);
                //?}
            }
        }
    }

    private DollScreenRender() {
    }
}
