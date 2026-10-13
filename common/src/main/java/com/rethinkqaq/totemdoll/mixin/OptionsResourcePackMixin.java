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

package com.rethinkqaq.totemdoll.mixin;

import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Options.class)
public abstract class OptionsResourcePackMixin {

    @Shadow @Final private List<String> resourcePacks;
    @Shadow @Final private List<String> incompatibleResourcePacks;

    @Inject(method = "loadSelectedResourcePacks", at = @At("HEAD"))
    private void totemdoll$selectGeneratedStyles(PackRepository repository, CallbackInfo callback) {
        var generatedPack = repository.getPack(DollLocalStyleStore.GENERATED_PACK_ID);
        if (generatedPack == null) {
            return;
        }

        if (!this.resourcePacks.contains(DollLocalStyleStore.GENERATED_PACK_ID)) {
            this.resourcePacks.add(DollLocalStyleStore.GENERATED_PACK_ID);
        }
        if (!generatedPack.getCompatibility().isCompatible()
                && !this.incompatibleResourcePacks.contains(DollLocalStyleStore.GENERATED_PACK_ID)) {
            this.incompatibleResourcePacks.add(DollLocalStyleStore.GENERATED_PACK_ID);
        }
    }

    //? if 1.21.1 {
    @Inject(method = "loadSelectedResourcePacks", at = @At("RETURN"))
    private void totemdoll$restoreGeneratedStylesSelection(PackRepository repository, CallbackInfo callback) {
        if (repository.getPack(DollLocalStyleStore.GENERATED_PACK_ID) != null) {
            repository.addPack(DollLocalStyleStore.GENERATED_PACK_ID);
        }
    }
    //?}
}
