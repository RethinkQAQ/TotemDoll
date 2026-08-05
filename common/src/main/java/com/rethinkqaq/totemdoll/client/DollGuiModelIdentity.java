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

package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.doll.DollStyle;
//? >= 1.21.8 {
/*import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
*///?} else if >= 1.21.6 {
/*import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
*///?}

public final class DollGuiModelIdentity {
    private DollGuiModelIdentity() {
    }

    public static void mark(Object renderState, Object context, DollStyle style) {
        //? >= 1.21.8 {
        /*if (!(renderState instanceof TrackingItemStackRenderState trackingState)
                || context != ItemDisplayContext.GUI) {
            return;
        }
        trackingState.appendModelIdentityElement(style.id());
        if (style.hasDynamicModel() || style.hasDynamicTextures() || style.supportsSkin()) {
            trackingState.setAnimated();
        }
        *///?} else if >= 1.21.6 {
        /*if (!(renderState instanceof ItemStackRenderState) || context != ItemDisplayContext.GUI) {
            return;
        }
        ((ItemStackRenderState) renderState).clearModelIdentity();
        ((ItemStackRenderState) renderState).appendModelIdentityElement(style.id());
        if (style.hasDynamicModel() || style.hasDynamicTextures() || style.supportsSkin()) {
            ((ItemStackRenderState) renderState).setAnimated();
        }
        *///?}
    }
}
