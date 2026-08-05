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

//? >= 1.21.4 {
/*import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.IdentityHashMap;
import java.util.Map;

// Associates 1.21.4's resolved hand state with the TotemDoll style it came from.
public final class DollThirdPersonState {
    private static final Map<ItemStackRenderState, DollStyle> STYLES = new IdentityHashMap<>();

    public static synchronized void mark(ItemStackRenderState renderState, ItemStack stack,
                                          ItemDisplayContext context) {
        if (DollPreviewContext.isNativeRender()) {
            STYLES.remove(renderState);
            return;
        }
        if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                && context != ItemDisplayContext.GROUND
                && context != ItemDisplayContext.FIXED
                && context != ItemDisplayContext.GUI
                && context != ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                && context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                //? >= 1.21.9 {
                /^&& context != ItemDisplayContext.ON_SHELF
                ^///?}
                ) {
            STYLES.remove(renderState);
            return;
        }

        if (!stack.is(Items.TOTEM_OF_UNDYING)) {
            STYLES.remove(renderState);
            return;
        }

        DollStyle style = DollPreviewContext.current();
        if (style == null) style = TotemDollConfig.selectedStyle();
        if (style != null && DollBoneModels.contains(style.id())) {
            STYLES.put(renderState, style);
            DollGuiModelIdentity.mark(renderState, context, style);
        } else {
            STYLES.remove(renderState);
        }
    }

    public static synchronized DollStyle get(ItemStackRenderState renderState) {
        return STYLES.get(renderState);
    }

    private DollThirdPersonState() {
    }
}
*///?} else {
public final class DollThirdPersonState {
    private DollThirdPersonState() {
    }
}
//?}
