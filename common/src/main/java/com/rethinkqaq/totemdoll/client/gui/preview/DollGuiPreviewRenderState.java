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

import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.utils.DollResourceId;

//? >= 1.21.6 {
/*import net.minecraft.client.gui.navigation.ScreenRectangle;
//? >= 26.1.2 {
/^import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
^///?} else {
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
//?}

public record DollGuiPreviewRenderState(
        DollStyle style, int x0, int y0, int x1, int y1, float scale,
        ScreenRectangle scissorArea, ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public DollGuiPreviewRenderState(DollStyle style, int x, int y, int width, int height, float scale) {
        this(style, x, y, width, height, scale, x, y, width, height);
    }

    public DollGuiPreviewRenderState(DollStyle style, int x, int y, int width, int height, float scale,
                                     int clipX, int clipY, int clipWidth, int clipHeight) {
        this(style, x, y, x + width, y + height, scale,
                new ScreenRectangle(clipX, clipY, Math.max(0, clipWidth), Math.max(0, clipHeight)),
                PictureInPictureRenderState.getBounds(x, y, x + width, y + height,
                        new ScreenRectangle(clipX, clipY, Math.max(0, clipWidth), Math.max(0, clipHeight))));
    }

    public PreviewKey key(int guiScale) {
        return new PreviewKey(style.id(), (x1 - x0) * guiScale, (y1 - y0) * guiScale, scale);
    }

    public boolean dynamic() {
        return style.hasDynamicModel() || style.hasDynamicTextures() || style.supportsSkin();
    }

    public record PreviewKey(DollResourceId styleId, int width, int height, float scale) {
    }
}
*///?} else {
public final class DollGuiPreviewRenderState {
    public DollGuiPreviewRenderState(DollStyle style, int x, int y, int width, int height, float scale) {
    }
    public DollGuiPreviewRenderState(DollStyle style, int x, int y, int width, int height, float scale,
                                     int clipX, int clipY, int clipWidth, int clipHeight) {
        this(style, x, y, width, height, scale);
    }
}
//?}
