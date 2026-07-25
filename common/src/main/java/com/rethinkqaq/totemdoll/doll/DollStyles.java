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

import com.rethinkqaq.totemdoll.Constants;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DollStyles {

    public static final ResourceLocation VANILLA_ID =
            ResourceLocation.withDefaultNamespace("default");
    public static final ResourceLocation ALEX_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "alex");

    public static final DollStyle VANILLA = new DollStyle(
            VANILLA_ID,
            "Vanilla Totem",
            "doll.totemdoll.vanilla",
            ResourceLocation.withDefaultNamespace("totem_of_undying"),
            false,
            null,
            false,
            null,
            DollStyleOrigin.BUILTIN,
            Map.of(), List.of(), "minecraft_item", null
    );

    private static final Map<ResourceLocation, DollStyle> STYLES = new LinkedHashMap<>();

    static {
        register(VANILLA);
    }

    public static void init() {
        Constants.LOG.info("Loaded {} Totem Doll styles", STYLES.size());
    }

    public static synchronized DollStyle get(ResourceLocation id) {
        return STYLES.getOrDefault(id, STYLES.getOrDefault(ALEX_ID, VANILLA));
    }

    public static synchronized List<DollStyle> all() {
        return List.copyOf(STYLES.values());
    }

    public static synchronized void replaceDiscovered(Collection<DollStyle> styles) {
        STYLES.clear();
        register(VANILLA);
        styles.forEach(DollStyles::register);
        Constants.LOG.info("Loaded {} Totem Doll styles", STYLES.size());
    }

    private static void register(DollStyle style) {
        if (STYLES.putIfAbsent(style.id(), style) != null) {
            throw new IllegalStateException("Duplicate doll style " + style.id());
        }
    }

    private DollStyles() {
    }
}
