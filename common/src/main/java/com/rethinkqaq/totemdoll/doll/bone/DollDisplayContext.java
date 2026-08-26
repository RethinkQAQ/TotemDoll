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

import java.util.Map;
import java.util.Set;

public final class DollDisplayContext {
    public static final String GUI = "gui";
    public static final String GROUND = "ground";
    public static final String FIXED = "fixed";
    public static final String FIRSTPERSON = "firstperson";
    public static final String FIRSTPERSON_RIGHTHAND = "firstperson_righthand";
    public static final String FIRSTPERSON_LEFTHAND = "firstperson_lefthand";
    public static final String THIRDPERSON = "thirdperson";
    public static final String THIRDPERSON_RIGHTHAND = "thirdperson_righthand";
    public static final String THIRDPERSON_LEFTHAND = "thirdperson_lefthand";
    public static final String HEAD = "head";
    public static final String ON_SHELF = "on_shelf";

    private static final Set<String> SUPPORTED = Set.of(
            GUI, GROUND, FIXED, FIRSTPERSON, FIRSTPERSON_RIGHTHAND, FIRSTPERSON_LEFTHAND,
            THIRDPERSON, THIRDPERSON_RIGHTHAND, THIRDPERSON_LEFTHAND, HEAD, ON_SHELF
    );

    public static boolean isSupported(String context) {
        return SUPPORTED.contains(context);
    }

    public static Set<String> supported() {
        return SUPPORTED;
    }

    public static DollDisplayTransform resolve(Map<String, DollDisplayTransform> displays, String context) {
        DollDisplayTransform display = displays.get(context);
        if (display == null && context.startsWith(THIRDPERSON + "_")) display = displays.get(THIRDPERSON);
        if (display == null && context.startsWith(FIRSTPERSON + "_")) display = displays.get(FIRSTPERSON);
        return display == null ? displays.get(FIXED) : display;
    }

    private DollDisplayContext() {}
}
