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

package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class TotemDollClient {

    private static boolean initialStylesReloaded;

    public static void init(Path configDirectory, Path gameDirectory) {
        DollStyles.init();
        TotemDollConfig.initialize(configDirectory);
        DollLocalStyleStore.initialize(configDirectory, gameDirectory);
        Minecraft client = Minecraft.getInstance();
        enableGeneratedPack(client);
        // The generated pack is enabled before the normal resource load, but
        // the resource manager may not have included it yet. Let the first
        // client tick perform one reload after the manager is ready so saved
        // local styles are visible on the first launch.
        initialStylesReloaded = false;
    }

    /** Reloads the generated pack once the client resource manager is ready. */
    public static boolean reloadInitialStylesIfReady() {
        if (initialStylesReloaded) {
            return true;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getResourceManager() == null) {
            return false;
        }
        initialStylesReloaded = true;
        reloadGeneratedStyles();
        return true;
    }

    public static CompletableFuture<Void> reloadGeneratedStyles() {
        Minecraft client = Minecraft.getInstance();
        enableGeneratedPack(client);
        return client.reloadResourcePacks();
    }

    private static void enableGeneratedPack(Minecraft client) {
        PackRepository repository = client.getResourcePackRepository();
        repository.reload();
        if (!repository.addPack(DollLocalStyleStore.GENERATED_PACK_ID)) {
            repository.setSelected(repository.getSelectedIds());
        }
    }

    private TotemDollClient() {
    }
}
