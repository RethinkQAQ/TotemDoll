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

/** A partial display transform used by a compatibility override. */
public record DollDisplayTransformPatch(DollDisplayVector rotation, DollDisplayVector translation,
                                        DollDisplayVector scale) {
    public DollDisplayTransform apply(DollDisplayTransform base) {
        DollDisplayTransform source = base == null ? DollDisplayTransform.IDENTITY : base;
        DollDisplayVector resolvedRotation = rotation == null
                ? new DollDisplayVector(source.rotationX(), source.rotationY(), source.rotationZ()) : rotation;
        DollDisplayVector resolvedTranslation = translation == null
                ? new DollDisplayVector(source.translationX(), source.translationY(), source.translationZ()) : translation;
        DollDisplayVector resolvedScale = scale == null
                ? new DollDisplayVector(source.scaleX(), source.scaleY(), source.scaleZ()) : scale;
        return new DollDisplayTransform(
                resolvedRotation.x(), resolvedRotation.y(), resolvedRotation.z(),
                resolvedTranslation.x(), resolvedTranslation.y(), resolvedTranslation.z(),
                resolvedScale.x(), resolvedScale.y(), resolvedScale.z()
        );
    }
}
