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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;

public final class DollLocalStyleStore {

    public static final String GENERATED_PACK_NAME = "totemdoll-generated";
    public static final String GENERATED_PACK_ID = "file/" + GENERATED_PACK_NAME;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path stylesDirectory;
    private static Path generatedPackDirectory;

    public static void initialize(Path configDirectory, Path gameDirectory) {
        stylesDirectory = configDirectory.resolve(Constants.MOD_ID).resolve("styles");
        generatedPackDirectory = gameDirectory.resolve("resourcepacks").resolve(GENERATED_PACK_NAME);
        try {
            Files.createDirectories(stylesDirectory);
            StylePackStore.initialize(stylesDirectory, generatedPackDirectory);
            rebuildGeneratedPack();
        } catch (IOException exception) {
            Constants.LOG.error("Could not initialize local doll styles", exception);
        }
    }

    public static DollResourceId importSkin(
            DollStyle template,
            Path sourceSkin,
            String displayName,
            ResourceManager resourceManager
    ) throws IOException {
        requireInitialized();
        if (!template.isTemplate() || !template.skin().supportsImport()) {
            throw new IOException("The selected doll style cannot be used as a skin template");
        }
        validateSkin(sourceSkin);

        return importMeshSkin(template, sourceSkin, displayName, resourceManager);
    }

    private static DollResourceId importMeshSkin(
            DollStyle template,
            Path sourceSkin,
            String displayName,
            ResourceManager resourceManager
    ) throws IOException {
        if (template.definitionSource() == null) {
            throw new IOException("Bone template has no source style definition");
        }
        JsonObject templateStyle = readResourceJson(resourceManager, template.definitionSource());
        JsonObject templateModel = templateStyle.getAsJsonObject("model");
        if (templateModel == null || !"mesh".equals(string(templateModel, "type"))) {
            throw new IOException("Template is not a mesh model");
        }
        String geometryPath = safeRelativePath(templateModel, "geometry");
        String animationsPath = templateModel.has("animations")
                ? safeRelativePath(templateModel, "animations") : null;
        JsonObject geometry = readResourceJson(
                resourceManager, resolveRelative(template.definitionSource(), geometryPath));
        JsonObject animations = animationsPath == null ? null : readResourceJson(
                resourceManager, resolveRelative(template.definitionSource(), animationsPath));

        String key = "user_" + UUID.randomUUID().toString().replace("-", "");
        DollResourceId id = DollResourceId.of(Constants.MOD_ID, key);
        JsonObject style = new JsonObject();
        style.addProperty("format", 3);
        style.addProperty("id", id.toString());
        style.addProperty("name", normalizeName(displayName, sourceSkin));
        style.addProperty("template", template.id().toString());
        style.addProperty("user_created", true);
        style.addProperty("origin", DollStyleOrigin.LOCAL.name().toLowerCase());

        JsonObject model = new JsonObject();
        model.addProperty("type", "mesh");
        model.addProperty("geometry", "models/geometry.json");
        if (animations != null) model.addProperty("animations", "models/animations.json");
        style.add("model", model);

        JsonObject textures = new JsonObject();
        textures.addProperty("base", "textures/skin.png");
        style.add("textures", textures);
        JsonObject skin = new JsonObject();
        skin.addProperty("supported", true);
        skin.addProperty("format", DollSkinDefinition.MINECRAFT_64X64);
        skin.addProperty("target", "base");
        skin.addProperty("mapping", "minecraft_player");
        style.add("skin", skin);
        if (templateStyle.has("features"))
            style.add("features", templateStyle.get("features").deepCopy());
        if (templateStyle.has("animations"))
            style.add("animations", templateStyle.get("animations").deepCopy());

        Path styleDirectory = stylesDirectory.resolve(key);
        Files.createDirectories(styleDirectory);
        Files.writeString(styleDirectory.resolve("style.json"), GSON.toJson(style), StandardCharsets.UTF_8);
        Files.writeString(styleDirectory.resolve("geometry.json"), GSON.toJson(geometry), StandardCharsets.UTF_8);
        if (animations != null)
            Files.writeString(styleDirectory.resolve("animations.json"), GSON.toJson(animations), StandardCharsets.UTF_8);
        Files.copy(sourceSkin, styleDirectory.resolve("skin.png"), StandardCopyOption.REPLACE_EXISTING);
        rebuildGeneratedPack();
        return id;
    }

    public static boolean rename(DollStyle style, String displayName) {
        if (!style.userCreated() || stylesDirectory == null) {
            return false;
        }
        String name = displayName == null ? "" : displayName.trim();
        if (name.isEmpty()) {
            return false;
        }
        Path directory = stylesDirectory.resolve(style.id().path()).normalize();
        Path metadataFile = directory.resolve("style.json");
        if (!directory.getParent().equals(stylesDirectory.normalize())
                || !Files.isRegularFile(metadataFile)) {
            return false;
        }
        try {
            JsonObject metadata = GSON.fromJson(
                    Files.readString(metadataFile, StandardCharsets.UTF_8),
                    JsonObject.class
            );
            metadata.addProperty("name", name);
            Files.writeString(metadataFile, GSON.toJson(metadata), StandardCharsets.UTF_8);
            rebuildGeneratedPack();
            return true;
        } catch (IOException exception) {
            Constants.LOG.error("Could not rename local doll style {}", style.id(), exception);
            return false;
        }
    }

    public static void validateSkinFile(Path path) throws IOException {
        validateSkin(path);
    }

    public static boolean delete(DollStyle style) {
        if (!style.userCreated() || stylesDirectory == null) {
            return false;
        }
        Path target = stylesDirectory.resolve(style.id().path()).normalize();
        if (!target.getParent().equals(stylesDirectory.normalize()) || !Files.isDirectory(target)) {
            return false;
        }
        try {
            deleteTree(target);
            rebuildGeneratedPack();
            return true;
        } catch (IOException exception) {
            Constants.LOG.error("Could not delete local doll style {}", style.id(), exception);
            return false;
        }
    }

    public static void rebuildGeneratedPack() throws IOException {
        requireInitialized();
        if (Files.exists(generatedPackDirectory)) {
            deleteTree(generatedPackDirectory);
        }
        Files.createDirectories(generatedPackDirectory);
        Files.writeString(
                generatedPackDirectory.resolve("pack.mcmeta"),
                """
                        {
                          "pack": {
                            "pack_format": 34,
                            "description": "Totem Doll generated local styles"
                          }
                        }
                        """,
                StandardCharsets.UTF_8
        );

        try (var directories = Files.list(stylesDirectory)) {
            directories.filter(Files::isDirectory)
                    // The imported-pack container belongs to the newer pack
                    // format and is not itself a local style directory.
                    .filter(path -> !path.getFileName().toString().equals("imported"))
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(DollLocalStyleStore::copyStyleToGeneratedPack);
        }
        StylePackStore.compileImported();
    }

    public static void importZip(Path zip) throws IOException {
        requireInitialized();
        StylePackStore.importZip(zip);
        rebuildGeneratedPack();
    }

    public static void importFolder(Path folder) throws IOException {
        requireInitialized();
        StylePackStore.importFolder(folder);
        rebuildGeneratedPack();
    }

    public static void exportStyle(DollStyle style, Path target) throws IOException {
        requireInitialized();
        StylePackStore.exportLocal(style, target);
    }

    private static void copyStyleToGeneratedPack(Path styleDirectory) {
        try {
            Path metadataFile = styleDirectory.resolve("style.json");
            Path skinFile = styleDirectory.resolve("skin.png");
            if (!Files.isRegularFile(metadataFile) || !Files.isRegularFile(skinFile)) {
                throw new IOException("Missing style.json or skin.png");
            }
            validateSkin(skinFile);
            JsonObject metadata = GSON.fromJson(
                    Files.readString(metadataFile, StandardCharsets.UTF_8),
                    JsonObject.class
            );
            if (metadata == null || !metadata.has("format") || metadata.get("format").getAsInt() != 3) {
                Constants.LOG.warn("Skipping obsolete local style {}: only style format 3 is supported", styleDirectory);
                return;
            }
            DollResourceId id = requireLocation(metadata, "id");
            if (!id.namespace().equals(Constants.MOD_ID)
                    || !id.path().equals(styleDirectory.getFileName().toString())) {
                throw new IOException("Style id does not match its directory");
            }
            JsonObject modelObject = metadata.getAsJsonObject("model");
            String modelType = string(modelObject, "type");

            Path assets = generatedPackDirectory.resolve("assets");
            Path styleTarget = assets.resolve(id.namespace()).resolve("styles/generated")
                    .resolve(id.path()).resolve("style.json");
            Path skinTarget = styleTarget.getParent().resolve("textures/skin.png");
            Files.createDirectories(styleTarget.getParent());
            Files.createDirectories(skinTarget.getParent());
            Files.copy(metadataFile, styleTarget, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(skinFile, skinTarget, StandardCopyOption.REPLACE_EXISTING);

            if ("mesh".equals(modelType)) {
                String geometryPath = safeRelativePath(modelObject, "geometry");
                copyBoneResource(styleDirectory.resolve("geometry.json"), styleTarget.getParent(), geometryPath);
                if (modelObject.has("animations")) {
                    String animationPath = safeRelativePath(modelObject, "animations");
                    copyBoneResource(styleDirectory.resolve("animations.json"), styleTarget.getParent(), animationPath);
                }
            } else {
                throw new IOException("Unsupported local model type " + modelType);
            }
        } catch (Exception exception) {
            // A damaged personal style must not prevent the client from
            // loading the remaining styles or starting the game.
            Constants.LOG.warn("Skipping invalid local doll style {}", styleDirectory, exception);
        }
    }

    private static void copyBoneResource(Path source, Path styleTargetDirectory, String relative) throws IOException {
        if (!Files.isRegularFile(source)) throw new IOException("Missing " + source.getFileName());
        Path target = styleTargetDirectory.resolve(relative).normalize();
        if (!target.startsWith(styleTargetDirectory.normalize())) throw new IOException("Invalid bone model path");
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static JsonObject readResourceJson(ResourceManager manager, DollResourceId location) throws IOException {
        Resource resource = manager.getResource(DollMinecraftResourceUtil.nativeId(location))
                .orElseThrow(() -> new IOException("Missing template resource " + location));
        try (Reader reader = resource.openAsReader()) {
            JsonObject value = GSON.fromJson(reader, JsonObject.class);
            if (value == null) throw new IOException("Template resource is empty " + location);
            return value;
        }
    }

    private static DollResourceId resolveRelative(DollResourceId source, String relative) {
        String parent = source.path().substring(0, source.path().lastIndexOf('/') + 1);
        return DollResourceId.of(source.namespace(), parent + relative);
    }

    private static String safeRelativePath(JsonObject object, String member) throws IOException {
        String value = string(object, member).replace('\\', '/');
        if (value.isBlank() || value.startsWith("/") || value.contains(".."))
            throw new IOException("Invalid relative path in " + member);
        return value;
    }

    private static String string(JsonObject object, String member) throws IOException {
        if (object == null || !object.has(member)) throw new IOException("Missing " + member);
        return object.get(member).getAsString();
    }

    private static void validateSkin(Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
            throw new IOException("Skin must be a valid 64x64 PNG image");
        }
    }

    private static DollResourceId requireLocation(JsonObject object, String member) throws IOException {
        if (object == null || !object.has(member)) {
            throw new IOException("Missing " + member);
        }
        DollResourceId location = DollResourceId.tryParse(object.get(member).getAsString());
        if (location == null) {
            throw new IOException("Invalid resource location in " + member);
        }
        return location;
    }

    private static String normalizeName(String displayName, Path sourceSkin) {
        String value = displayName == null ? "" : displayName.trim();
        if (!value.isEmpty()) {
            return value;
        }
        String fileName = sourceSkin.getFileName().toString();
        int extension = fileName.lastIndexOf('.');
        return extension > 0 ? fileName.substring(0, extension) : fileName;
    }

    private static void deleteTree(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void requireInitialized() {
        if (stylesDirectory == null || generatedPackDirectory == null) {
            throw new IllegalStateException("Local doll style store is not initialized");
        }
    }

    private DollLocalStyleStore() {
    }
}
