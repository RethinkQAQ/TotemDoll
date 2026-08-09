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

import com.rethinkqaq.totemdoll.utils.DollGuiGraphics;
//? >= 26.1.2 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Predicate;

abstract class DollScreen extends Screen {
    //? >= 26.1.2 {
    private GuiGraphicsExtractor activeGraphics;
    //?} else {
    /*private GuiGraphics activeGraphics;
    *///?}

    protected DollScreen(Component title) {
        super(title);
    }

    //? >= 26.1.2 {
    @Override
    public final void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        activeGraphics = graphics;
        try {
            extractTransparentBackground(graphics);
            renderContent(DollGuiGraphics.wrap(graphics), mouseX, mouseY, partialTick);
        } finally {
            activeGraphics = null;
        }
    }
    //?} else {
    /*@Override
    public final void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        activeGraphics = graphics;
        try {
            renderTransparentBackground(graphics);
            renderContent(DollGuiGraphics.wrap(graphics), mouseX, mouseY, partialTick);
        } finally {
            activeGraphics = null;
        }
    }
    *///?}

    protected abstract void renderContent(DollGuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    protected final void renderChildren(int mouseX, int mouseY, float partialTick) {
        renderChildren(mouseX, mouseY, partialTick, child -> true);
    }

    protected final void renderChildren(
            int mouseX, int mouseY, float partialTick, Predicate<GuiEventListener> filter
    ) {
        if (activeGraphics == null) {
            return;
        }
        DollScreenRender.renderChildren(this, activeGraphics, mouseX, mouseY, partialTick, filter);
    }
}
