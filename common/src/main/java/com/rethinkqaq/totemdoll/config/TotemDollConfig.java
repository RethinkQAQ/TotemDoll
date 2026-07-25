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

package com.rethinkqaq.totemdoll.config;

import com.rethinkqaq.totemdoll.Constants;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TotemDollConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file;
    private static ResourceLocation selectedStyle = DollStyles.ALEX_ID;

    public static void initialize(Path configDirectory) {
        file = configDirectory.resolve("totemdoll-client.json");
        load();
    }

    public static DollStyle selectedStyle() {
        return DollStyles.get(selectedStyle);
    }

    public static void select(ResourceLocation styleId) {
        selectedStyle = DollStyles.get(styleId).id();
        save();
    }

    private static void load() {
        if (file == null || !Files.isRegularFile(file)) {
            save();
            return;
        }

        try {
            JsonObject root = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
            if (root != null && root.has("selected_style")) {
                ResourceLocation parsed = ResourceLocation.tryParse(root.get("selected_style").getAsString());
                if (parsed != null) {
                    selectedStyle = parsed;
                }
            }
        } catch (Exception exception) {
            Constants.LOG.warn("Could not read {}, using Alex", file, exception);
            selectedStyle = DollStyles.ALEX_ID;
        }
    }

    private static void save() {
        if (file == null) {
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("format", 1);
        root.addProperty("selected_style", selectedStyle.toString());

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            Constants.LOG.error("Could not save {}", file, exception);
        }
    }

    private TotemDollConfig() {
    }
}
