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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rethinkqaq.totemdoll.Constants;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import com.rethinkqaq.totemdoll.utils.DollResourceId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Process-wide TotemDoll configuration lifecycle. The generated wrapper owns YAML persistence. */
public final class TotemDollConfigRuntime {
    public static final float MIN_SKIN_LAYER_THICKNESS = 0.05F;
    public static final float MAX_SKIN_LAYER_THICKNESS = 1.0F;
    public static final float MIN_SKIN_LAYER_DISTANCE = 0.0F;
    public static final float MAX_SKIN_LAYER_DISTANCE = 64.0F;

    private static final Gson GSON = new GsonBuilder().create();
    private static TotemDollConfig config;
    public static synchronized void initialize(Path configDirectory) {
        Path yaml = configDirectory.resolve("totemdoll.yaml");
        Path legacy = configDirectory.resolve("totemdoll-client.json");
        boolean migrate = !Files.isRegularFile(yaml) && Files.isRegularFile(legacy);
        try {
            config = TotemDollConfig.createAndLoad(configDirectory);
            if (migrate) migrateLegacy(legacy);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load TotemDoll configuration", exception);
        }
    }

    private static void migrateLegacy(Path legacy) {
        try {
            JsonObject root = GSON.fromJson(Files.readString(legacy, StandardCharsets.UTF_8), JsonObject.class);
            if (root == null) return;
            if (root.has("selected_style")) {
                DollResourceId id = DollResourceId.tryParse(root.get("selected_style").getAsString());
                if (id != null) config.selectedStyle().set(id);
            }
            if (root.has("skin_layer_3d") && root.get("skin_layer_3d").isJsonObject()) {
                JsonObject layer = root.getAsJsonObject("skin_layer_3d");
                if (layer.has("enabled")) config.skinLayer3dEnabled().set(layer.get("enabled").getAsBoolean());
                if (layer.has("thickness")) config.skinLayer3dThickness().set(layer.get("thickness").getAsFloat());
                if (layer.has("fallback_distance")) config.skinLayer3dDistance().set(layer.get("fallback_distance").getAsFloat());
            }
            config.flush();
            Constants.LOG.info("Migrated TotemDoll configuration from {} to YAML", legacy.getFileName());
        } catch (Exception exception) {
            Constants.LOG.warn("Could not migrate legacy TotemDoll configuration; using defaults", exception);
        }
    }

    private static TotemDollConfig require() {
        if (config == null) throw new IllegalStateException("TotemDoll configuration has not been initialized");
        return config;
    }

    public static DollStyle selectedStyle() {
        DollResourceId id = require().selectedStyle().get();
        DollStyle style = DollStyles.find(id);
        if (style != null) return style;
        return DollStyles.find(fallbackStyle());
    }

    public static DollResourceId selectedStyleId() { return require().selectedStyle().get(); }

    public static void select(DollResourceId styleId) {
        if (styleId == null || !DollStyles.contains(styleId)) {
            Constants.LOG.warn("Ignoring unknown Totem Doll style {}", styleId);
            return;
        }
        DollStyle style = DollStyles.find(styleId);
        if (!style.isAvailable()) {
            Constants.LOG.warn("Ignoring unavailable Totem Doll style {}", styleId);
            return;
        }
        require().selectedStyle().set(styleId);
        Constants.LOG.info("Selected Totem Doll style {}", styleId);
    }

    public static void reconcileSelectedStyle() {
        // Keep a missing imported ID intact. The UI can show the missing state and the host decides whether to reset it.
    }

    public static boolean skinLayer3dEnabled() { return require().skinLayer3dEnabled().get(); }
    public static float skinLayer3dThickness() { return require().skinLayer3dThickness().get(); }
    public static float skinLayer3dDistance() { return require().skinLayer3dDistance().get(); }
    public static void setSkinLayer3dEnabled(boolean value) { require().skinLayer3dEnabled().set(value); }
    public static void setSkinLayer3dThickness(float value) { require().skinLayer3dThickness().set(value); }
    public static void setSkinLayer3dDistance(float value) { require().skinLayer3dDistance().set(value); }
    public static void flush() {
        try { require().flush(); } catch (IOException exception) { Constants.LOG.error("Could not flush TotemDoll configuration", exception); }
    }
    public static void close() {
        if (config == null) return;
        try { config.close(); } catch (IOException exception) { Constants.LOG.error("Could not close TotemDoll configuration", exception); }
        config = null;
    }

    private static DollResourceId fallbackStyle() {
        return DollStyles.contains(DollStyles.ALEX_ID) ? DollStyles.ALEX_ID : DollStyles.VANILLA_ID;
    }

    private TotemDollConfigRuntime() {
    }
}
