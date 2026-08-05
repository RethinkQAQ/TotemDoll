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

//? >= 1.21.11 {
/*import net.minecraft.resources.Identifier;
*///? } else {
import net.minecraft.resources.ResourceLocation;
//? }

public final class DollMinecraftResourceUtil {
    private DollMinecraftResourceUtil() {
    }

    //? >= 1.21.11 {
    /*public static Identifier nativeId(DollResourceId id) {
        return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
    }

    public static DollResourceId fromNative(Identifier id) {
        return DollResourceId.of(id.getNamespace(), id.getPath());
    }

    public static String namespace(Identifier id) {
        return id.getNamespace();
    }

    public static String path(Identifier id) {
        return id.getPath();
    }

    public static Identifier resolve(Identifier source, String relative) {
        return nativeId(DollResourceId.resolve(fromNative(source), relative));
    }
    *///? } else {
    public static ResourceLocation nativeId(DollResourceId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    public static DollResourceId fromNative(ResourceLocation id) {
        return DollResourceId.of(id.getNamespace(), id.getPath());
    }

    public static String namespace(ResourceLocation id) {
        return id.getNamespace();
    }

    public static String path(ResourceLocation id) {
        return id.getPath();
    }

    public static ResourceLocation resolve(ResourceLocation source, String relative) {
        return nativeId(DollResourceId.resolve(fromNative(source), relative));
    }
    //?}

    public static DollResourceId parse(String value) {
        return DollResourceId.parse(value);
    }

    public static DollResourceId parseNativeId(String value) {
        return DollResourceId.parse(value);
    }
}
