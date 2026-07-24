package com.example.examplemod.config;

import com.example.examplemod.Constants;
import com.example.examplemod.doll.DollStyle;
import com.example.examplemod.doll.DollStyles;
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
