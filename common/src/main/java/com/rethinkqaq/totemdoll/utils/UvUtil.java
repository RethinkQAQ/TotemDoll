/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 *
 * Totem Doll is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 */

package com.rethinkqaq.totemdoll.utils;

import com.rethinkqaq.totemdoll.doll.bone.DollFace;

/**
 * UV helpers for TotemDoll's version-independent mesh format.
 *
 * The format stores a rectangular face UV and a clockwise rotation in degrees.
 * Keeping this logic here avoids depending on Minecraft's native model classes.
 */
public final class UvUtil {
    private UvUtil() {
    }

    public static float[] vertexUv(DollFace face, int vertex) {
        int rotation = Math.floorMod(face.rotation(), 360) / 90;
        int index = Math.floorMod(vertex + rotation, 4);

        return switch (index) {
            case 0 -> new float[]{face.u1(), face.v1()};
            case 1 -> new float[]{face.u1(), face.v2()};
            case 2 -> new float[]{face.u2(), face.v2()};
            case 3 -> new float[]{face.u2(), face.v1()};
            default -> throw new AssertionError(index);
        };
    }
}
