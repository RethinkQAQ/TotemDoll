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

import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prepares the generated directory before vanilla discovers directory packs. */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    private static boolean totemdoll$prepared;

    @Inject(method = "reload", at = @At("HEAD"))
    private void totemdoll$prepareGeneratedStyles(CallbackInfo callback) {
        if (totemdoll$prepared) {
            return;
        }
        totemdoll$prepared = true;
        DollLocalStyleStore.initialize(
                FabricLoader.getInstance().getConfigDir(),
                FabricLoader.getInstance().getGameDir()
        );
    }
}
