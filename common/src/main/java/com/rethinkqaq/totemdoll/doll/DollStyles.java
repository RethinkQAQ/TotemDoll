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
import com.rethinkqaq.totemdoll.utils.DollResourceId;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DollStyles {

    public static final DollResourceId VANILLA_ID =
            DollResourceId.ofVanilla("default");
    public static final DollResourceId ALEX_ID =
            DollResourceId.of(Constants.MOD_ID, "alex");

    public static final DollStyle VANILLA = new DollStyle(
            VANILLA_ID,
            "Vanilla Totem",
            "doll.totemdoll.vanilla",
            DollResourceId.ofVanilla("totem_of_undying"),
            false,
            null,
            false,
            null,
            DollStyleOrigin.BUILTIN,
            Map.of(), List.of(), "mesh", null, null, null
    );

    private static final Map<DollResourceId, DollStyle> STYLES = new LinkedHashMap<>();

    static {
        register(VANILLA);
    }

    public static void init() {
        Constants.LOG.info("Loaded {} Totem Doll styles", STYLES.size());
    }

    public static synchronized DollStyle get(DollResourceId id) {
        return STYLES.getOrDefault(id, STYLES.getOrDefault(ALEX_ID, VANILLA));
    }

    /** Returns the exact registered style, or {@code null} when an imported id is missing. */
    public static synchronized DollStyle find(DollResourceId id) {
        return id == null ? null : STYLES.get(id);
    }

    public static synchronized List<DollStyle> all() {
        return List.copyOf(STYLES.values());
    }

    public static synchronized boolean contains(DollResourceId id) {
        return STYLES.containsKey(id);
    }

    public static synchronized void replaceDiscovered(Collection<DollStyle> styles) {
        STYLES.clear();
        register(VANILLA);
        for (DollStyle style : styles) {
            if (style == null || VANILLA_ID.equals(style.id())) continue;
            register(style);
        }
        Constants.LOG.info("Loaded {} Totem Doll styles", STYLES.size());
    }

    private static void register(DollStyle style) {
        if (style == null || style.id() == null) return;
        if (STYLES.putIfAbsent(style.id(), style) != null) {
            Constants.LOG.warn("Ignoring duplicate Totem Doll style {}", style.id());
        }
    }

    private DollStyles() {
    }
}
