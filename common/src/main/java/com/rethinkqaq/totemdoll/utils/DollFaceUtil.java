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

import net.minecraft.client.renderer.FaceInfo;

public final class DollFaceUtil {
    private DollFaceUtil() {
    }

    public static float x(FaceInfo.VertexInfo vertex, float[] shape,
                          float minX, float minY, float minZ,
                          float maxX, float maxY, float maxZ) {
        //? >= 1.21.11 {
        /*return vertex.xFace().select(minX, minY, minZ, maxX, maxY, maxZ);
        *///?} else {
        return shape[vertex.xFace];
        //?}
    }

    public static float y(FaceInfo.VertexInfo vertex, float[] shape,
        float minX, float minY, float minZ,
                          float maxX, float maxY, float maxZ) {
        //? >= 1.21.11 {
        /*return vertex.yFace().select(minX, minY, minZ, maxX, maxY, maxZ);
        *///?} else {
        return shape[vertex.yFace];
        //?}
    }

    public static float z(FaceInfo.VertexInfo vertex, float[] shape,
        float minX, float minY, float minZ,
                          float maxX, float maxY, float maxZ) {
        //? >= 1.21.11 {
        /*return vertex.zFace().select(minX, minY, minZ, maxX, maxY, maxZ);
        *///?} else {
        return shape[vertex.zFace];
        //?}
    }
}
