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
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

abstract class DollWidget extends AbstractWidget {
    protected DollWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    //? >= 26.1.2 {
    /*@Override
    protected final void extractWidgetRenderState(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick
    ) {
        renderContent(DollGuiGraphics.wrap(graphics), mouseX, mouseY, partialTick);
    }
    *///?} else {
    @Override
    protected final void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderContent(DollGuiGraphics.wrap(graphics), mouseX, mouseY, partialTick);
    }
    //?}

    protected abstract void renderContent(DollGuiGraphics graphics, int mouseX, int mouseY, float partialTick);
}
