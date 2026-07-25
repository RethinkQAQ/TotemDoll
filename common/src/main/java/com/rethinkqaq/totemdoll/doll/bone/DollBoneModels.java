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

package com.rethinkqaq.totemdoll.doll.bone;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class DollBoneModels {
    private static final Map<ResourceLocation, DollBoneModel> MODELS = new HashMap<>();

    public static synchronized void put(ResourceLocation styleId, DollBoneModel model) { MODELS.put(styleId, model); }
    public static synchronized DollBoneModel get(ResourceLocation styleId) { return MODELS.get(styleId); }
    public static synchronized boolean contains(ResourceLocation styleId) { return MODELS.containsKey(styleId); }
    public static synchronized void clear() { MODELS.clear(); }

    private DollBoneModels() {}
}
