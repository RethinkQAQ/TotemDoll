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

import net.minecraft.client.gui.GuiGraphics;

/** Version-independent GUI pose operations. */
public final class GuiPoseUtil {
    private GuiPoseUtil() {
    }

    public static void push(GuiGraphics graphics) {
        //? >= 1.21.6 {
        /*graphics.pose().pushMatrix();
        *///?} else {
        graphics.pose().pushPose();
        //?}
    }

    public static void pop(GuiGraphics graphics) {
        //? >= 1.21.6 {
        /*graphics.pose().popMatrix();
        *///?} else {
        graphics.pose().popPose();
        //?}
    }

    public static void translate(GuiGraphics graphics, float x, float y, float z) {
        //? >= 1.21.6 {
        /*graphics.pose().translate(x, y);
        *///?} else {
        graphics.pose().translate(x, y, z);
        //?}
    }

    public static void scale(GuiGraphics graphics, float x, float y, float z) {
        //? >= 1.21.6 {
        /*graphics.pose().scale(x, y);
        *///?} else {
        graphics.pose().scale(x, y, z);
        //?}
    }
}
