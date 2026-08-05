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

import java.util.Objects;

/** Version-independent resource identifier used by TotemDoll data models. */
public record DollResourceId(String value) {
    public DollResourceId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Resource identifier must not be blank");
        }

        String namespace = namespaceOf(value);
        String path = pathOf(value);
        value = namespace + ":" + path;
        if (!namespace.matches("[a-z0-9_.-]+") || path.isBlank()
                || !path.matches("[a-z0-9/._-]+") || path.contains("..")) {
            throw new IllegalArgumentException("Invalid resource identifier: " + value);
        }
    }

    public static DollResourceId parse(String value) {
        return new DollResourceId(value);
    }

    public static DollResourceId tryParse(String value) {
        try {
            return parse(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static DollResourceId of(String namespace, String path) {
        return new DollResourceId(namespace + ":" + path);
    }

    public static DollResourceId ofVanilla(String path) {
        return of("minecraft", path);
    }

    public String namespace() {
        return namespaceOf(value);
    }

    public String path() {
        return pathOf(value);
    }

    public DollResourceId resolve(String relative) {
        Objects.requireNonNull(relative, "relative");
        if (relative.isBlank() || relative.startsWith("/") || relative.contains("..")) {
            throw new IllegalArgumentException("Invalid relative resource path: " + relative);
        }

        int separator = path().lastIndexOf('/');
        String parent = separator < 0 ? "" : path().substring(0, separator + 1);
        return of(namespace(), parent + relative);
    }

    public static DollResourceId resolve(DollResourceId source, String relative) {
        return Objects.requireNonNull(source, "source").resolve(relative);
    }

    private static String namespaceOf(String value) {
        int separator = value.indexOf(':');
        return separator < 0 ? "minecraft" : value.substring(0, separator);
    }

    private static String pathOf(String value) {
        int separator = value.indexOf(':');
        return separator < 0 ? value : value.substring(separator + 1);
    }

    @Override
    public String toString() {
        return namespace() + ":" + path();
    }
}
