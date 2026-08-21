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
import com.rethinkqaq.totemdoll.utils.DollResourceId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TotemDollConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final float MIN_SKIN_LAYER_THICKNESS = 0.05F;
    public static final float MAX_SKIN_LAYER_THICKNESS = 1.0F;
    public static final float MIN_SKIN_LAYER_DISTANCE = 0.0F;
    public static final float MAX_SKIN_LAYER_DISTANCE = 64.0F;
    private static Path file;
    private static DollResourceId selectedStyle = DollStyles.ALEX_ID;
    private static boolean skinLayer3dEnabled;
    private static float skinLayer3dThickness = 0.25F;
    private static float skinLayer3dDistance = 12.0F;

    public static void initialize(Path configDirectory) {
        file = configDirectory.resolve("totemdoll-client.json");
        load();
    }

    public static DollStyle selectedStyle() {
        return DollStyles.get(selectedStyle);
    }

    public static void select(DollResourceId styleId) {
        selectedStyle = DollStyles.contains(styleId) ? styleId : fallbackStyle();
        Constants.LOG.info("Selected Totem Doll style {}", selectedStyle);
        save();
    }

    /** Keeps a valid selection across a temporary empty style registry during reload. */
    public static void reconcileSelectedStyle() {
        if (!DollStyles.contains(selectedStyle)) {
            selectedStyle = fallbackStyle();
            Constants.LOG.warn("Selected Totem Doll style was unavailable; fell back to {}", selectedStyle);
            save();
        }
    }

    public static boolean skinLayer3dEnabled() { return skinLayer3dEnabled; }
    public static float skinLayer3dThickness() { return skinLayer3dThickness; }
    public static float skinLayer3dDistance() { return skinLayer3dDistance; }

    public static void setSkinLayer3dEnabled(boolean enabled) {
        skinLayer3dEnabled = enabled;
        save();
    }

    public static void setSkinLayer3dThickness(float thickness) {
        skinLayer3dThickness = clamp(thickness, MIN_SKIN_LAYER_THICKNESS, MAX_SKIN_LAYER_THICKNESS);
        save();
    }

    public static void setSkinLayer3dDistance(float distance) {
        skinLayer3dDistance = clamp(distance, MIN_SKIN_LAYER_DISTANCE, MAX_SKIN_LAYER_DISTANCE);
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
                DollResourceId parsed = DollResourceId.tryParse(root.get("selected_style").getAsString());
                if (parsed != null) {
                    selectedStyle = parsed;
                }
            }
            if (root != null && root.has("skin_layer_3d")) {
                JsonObject layer = root.getAsJsonObject("skin_layer_3d");
                if (layer.has("enabled")) skinLayer3dEnabled = layer.get("enabled").getAsBoolean();
                if (layer.has("thickness")) skinLayer3dThickness = clamp(
                        layer.get("thickness").getAsFloat(), MIN_SKIN_LAYER_THICKNESS, MAX_SKIN_LAYER_THICKNESS);
                if (layer.has("fallback_distance")) skinLayer3dDistance = clamp(
                        layer.get("fallback_distance").getAsFloat(), MIN_SKIN_LAYER_DISTANCE, MAX_SKIN_LAYER_DISTANCE);
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
        root.addProperty("format", 2);
        root.addProperty("selected_style", selectedStyle.toString());
        JsonObject layer = new JsonObject();
        layer.addProperty("enabled", skinLayer3dEnabled);
        layer.addProperty("thickness", skinLayer3dThickness);
        layer.addProperty("fallback_distance", skinLayer3dDistance);
        root.add("skin_layer_3d", layer);

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            Constants.LOG.error("Could not save {}", file, exception);
        }
    }

    private static DollResourceId fallbackStyle() {
        return DollStyles.contains(DollStyles.ALEX_ID) ? DollStyles.ALEX_ID : DollStyles.VANILLA_ID;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private TotemDollConfig() {
    }
}
