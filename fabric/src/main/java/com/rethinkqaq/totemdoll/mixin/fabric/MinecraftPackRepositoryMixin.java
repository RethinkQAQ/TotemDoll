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

package com.rethinkqaq.totemdoll.mixin.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rethinkqaq.totemdoll.doll.DollGeneratedPackSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(Minecraft.class)
public abstract class MinecraftPackRepositoryMixin {

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "Lnet/minecraft/server/packs/repository/PackRepository;"))
    private PackRepository totemdoll$registerGeneratedStyles(RepositorySource[] sources, Operation<PackRepository> original) {
        RepositorySource[] registeredSources = Arrays.copyOf(sources, sources.length + 1);
        FabricLoader loader = FabricLoader.getInstance();
        registeredSources[sources.length] = new DollGeneratedPackSource(loader.getConfigDir(), loader.getGameDir());
        return original.call((Object) registeredSources);
    }
}
