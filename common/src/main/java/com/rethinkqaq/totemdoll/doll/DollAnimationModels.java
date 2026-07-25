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

package com.rethinkqaq.totemdoll.doll;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class DollAnimationModels {
    public static List<ResourceLocation> modelIds(DollStyle style) {
        List<ResourceLocation> ids = new ArrayList<>();
        for (DollAnimationDefinition animation : style.animations()) {
            for (String frame : animation.frames()) {
                ids.add(frameModelId(style, frame));
            }
        }
        return ids.stream().distinct().toList();
    }

    public static ResourceLocation frameModelId(DollStyle style, String frame) {
        return ResourceLocation.fromNamespaceAndPath(style.model().getNamespace(),
                style.model().getPath() + "__" + frame);
    }

    private DollAnimationModels() {}
}
