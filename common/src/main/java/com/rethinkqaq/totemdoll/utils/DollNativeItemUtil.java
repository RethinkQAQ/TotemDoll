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

import net.minecraft.world.item.ItemStack;
//? if 26.1.2 {
/*import net.minecraft.client.Minecraft;
*///?}
//? >= 26.2 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
*///?} else {
import net.minecraft.world.item.Items;
//?}

public final class DollNativeItemUtil {
    //? >= 26.2 {
    /*private static final ResourceKey<Item> TOTEM_OF_UNDYING = ResourceKey.create(
            Registries.ITEM, Identifier.withDefaultNamespace("totem_of_undying")
    );
    *///?}

    public static ItemStack createTotemStack() {
        //? if 26.1.2 {
        /*if (Minecraft.getInstance().level == null) {
            return ItemStack.EMPTY;
        }
        *///?}
        //? >= 26.2 {
        /*var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(connection.registryAccess()
                .lookupOrThrow(Registries.ITEM)
                .getOrThrow(TOTEM_OF_UNDYING));
        *///?} else {
        return new ItemStack(Items.TOTEM_OF_UNDYING);
        //?}
    }

    private DollNativeItemUtil() {
    }
}
