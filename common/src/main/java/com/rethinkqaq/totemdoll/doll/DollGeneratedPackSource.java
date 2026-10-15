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

package com.rethinkqaq.totemdoll.doll;

import com.rethinkqaq.totemdoll.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/** Exposes local styles as a required client pack, independently of saved pack selections. */
public final class DollGeneratedPackSource implements RepositorySource {

    private final Path configDirectory;
    private final Path gameDirectory;

    public DollGeneratedPackSource(Path configDirectory, Path gameDirectory) {
        this.configDirectory = configDirectory;
        this.gameDirectory = gameDirectory;
    }

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        try {
            Path directory = DollLocalStyleStore.initialize(configDirectory, gameDirectory);
            Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo(DollLocalStyleStore.GENERATED_PACK_ID,
                            Component.literal("Totem Doll generated local styles"), PackSource.BUILT_IN, Optional.empty()),
                    new PathPackResources.PathResourcesSupplier(directory),
                    PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.TOP, false)
            );
            if (pack == null) {
                Constants.LOG.error("Could not read generated Totem Doll pack metadata in {}", directory);
                return;
            }
            consumer.accept(pack);
        } catch (IOException exception) {
            Constants.LOG.error("Could not prepare generated Totem Doll styles", exception);
        }
    }
}
