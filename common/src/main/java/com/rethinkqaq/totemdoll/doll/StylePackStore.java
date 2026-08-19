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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rethinkqaq.totemdoll.Constants;
import com.rethinkqaq.totemdoll.utils.DollResourceId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Portable style-pack import/export storage for the version-independent format-3 runtime. */
public final class StylePackStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long MAX_BYTES = 64L * 1024L * 1024L;
    private static Path stylesDirectory;
    private static Path generatedPackDirectory;

    public static void initialize(Path styles, Path generated) throws IOException {
        stylesDirectory = styles;
        generatedPackDirectory = generated;
        Files.createDirectories(stylesDirectory.resolve("imported"));
    }

    public static void importZip(Path zip) throws IOException {
        String key = uniqueKey(zip.getFileName().toString().replaceFirst("\\.[^.]+$", ""));
        Path target = stylesDirectory.resolve("imported").resolve(key);
        Files.createDirectories(target);
        long total = 0;
        try (InputStream input = Files.newInputStream(zip); ZipInputStream archive = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                Path file = safeResolve(target, entry.getName());
                Files.createDirectories(file.getParent());
                total += Files.copy(archive, file, StandardCopyOption.REPLACE_EXISTING);
                if (total > MAX_BYTES) throw new IOException("Style pack exceeds 64 MiB");
            }
        } catch (Exception exception) {
            deleteTree(target);
            throw exception;
        }
        try {
            validateImportedPack(target);
        } catch (Exception exception) {
            deleteTree(target);
            throw exception;
        }
    }

    public static void importFolder(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) throw new IOException("Selected folder does not exist");
        String key = uniqueKey(folder.getFileName().toString());
        Path target = stylesDirectory.resolve("imported").resolve(key);
        copyTree(folder, target);
        try {
            validateImportedPack(target);
        } catch (Exception exception) {
            deleteTree(target);
            throw exception;
        }
    }

    public static void compileImported() throws IOException {
        Path imported = stylesDirectory.resolve("imported");
        if (!Files.isDirectory(imported)) return;
        try (var packs = Files.list(imported)) {
            for (Path pack : packs.filter(Files::isDirectory).toList()) {
                try (var styles = Files.walk(pack)) {
                    for (Path style : styles.filter(path -> path.getFileName().toString().equals("style.json")).toList()) {
                        compileStyle(pack.getFileName().toString(), style);
                    }
                } catch (Exception exception) {
                    Constants.LOG.warn("Skipping imported style pack {}", pack, exception);
                }
            }
        }
    }

    private static void compileStyle(String packKey, Path styleFile) throws IOException {
        JsonObject style = read(styleFile);
        if (!style.has("format") || style.get("format").getAsInt() != 3) throw new IOException("Expected format 3");
        DollResourceId id = DollResourceId.tryParse(style.get("id").getAsString());
        if (id == null) throw new IOException("Invalid style id");
        String key = safeName(packKey) + "/" + safeName(id.path());
        Path root = styleFile.getParent();
        JsonObject model = style.getAsJsonObject("model");
        if (model == null) throw new IOException("Missing model");
        String type = model.get("type").getAsString();
        if (!"mesh".equals(type)) throw new IOException("Unsupported model type " + type);
        Path styleTarget = generatedPackDirectory.resolve("assets").resolve(id.namespace())
                .resolve("styles/imported").resolve(key).resolve("style.json");
        copyModelFile(root, model, "geometry", styleTarget.getParent().resolve("models/geometry.json"));
        if (model.has("animations")) copyModelFile(root, model, "animations", styleTarget.getParent().resolve("models/animations.json"));
        model.addProperty("geometry", "models/geometry.json");
        if (model.has("animations")) model.addProperty("animations", "models/animations.json");
        JsonObject textures = style.getAsJsonObject("textures");
        if (textures == null || !textures.has("base")) throw new IOException("Missing textures.base");
        for (String slot : textures.keySet()) {
            String value = safePath(textures, slot);
            Path target = styleTarget.getParent().resolve(value).normalize();
            if (!target.startsWith(styleTarget.getParent().normalize())) throw new IOException("Invalid texture path");
            Files.createDirectories(target.getParent());
            Files.copy(safeResolve(root, value), target, StandardCopyOption.REPLACE_EXISTING);
        }
        style.addProperty("origin", "imported");
        write(styleTarget, style);
    }


    public static void exportLocal(DollStyle style, Path zip) throws IOException {
        if (!style.isLocal()) throw new IOException("Only local styles can be exported");
        Path source = stylesDirectory.resolve(style.id().path()).normalize();
        if (!source.startsWith(stylesDirectory) || !Files.isDirectory(source)) throw new IOException("Local style not found");
        try (OutputStream output = Files.newOutputStream(zip); ZipOutputStream archive = new ZipOutputStream(output)) {
            JsonObject exportedStyle = read(source.resolve("style.json"));
            JsonObject model = exportedStyle.getAsJsonObject("model");
            String modelType = model.get("type").getAsString();
            if (!"mesh".equals(modelType)) throw new IOException("Unsupported model type " + modelType);
            model.addProperty("geometry", "models/geometry.json");
            if (model.has("animations")) model.addProperty("animations", "models/animations.json");
            JsonObject textures = exportedStyle.getAsJsonObject("textures");
            if (textures == null) { textures = new JsonObject(); exportedStyle.add("textures", textures); }
            textures.entrySet().clear();
            textures.addProperty("base", "textures/skin.png");
            JsonObject pack = new JsonObject();
            pack.addProperty("format", 1);
            pack.addProperty("id", style.id().toString() + "_pack");
            pack.addProperty("name", style.displayName());
            pack.addProperty("author", "Totem Doll");
            JsonArray styles = new JsonArray();
            styles.add("styles/" + style.id().path() + "/style.json");
            pack.add("styles", styles);
            addEntry(archive, "pack.json", GSON.toJson(pack).getBytes(StandardCharsets.UTF_8));
            String base = "styles/" + style.id().path() + "/";
            addEntry(archive, base + "style.json", GSON.toJson(exportedStyle).getBytes(StandardCharsets.UTF_8));
            addEntry(archive, base + "models/geometry.json", Files.readAllBytes(source.resolve("geometry.json")));
            if (model.has("animations")) addEntry(archive, base + "models/animations.json", Files.readAllBytes(source.resolve("animations.json")));
            addEntry(archive, base + "textures/skin.png", Files.readAllBytes(source.resolve("skin.png")));
        }
    }

    private static void copyModelFile(Path root, JsonObject model, String key, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(safeResolve(root, safePath(model, key)), target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean hasPackManifest(Path root) {
        return Files.isRegularFile(root.resolve("pack.json")) || Files.isRegularFile(root.resolve("style.json"));
    }

    private static void validateImportedPack(Path target) throws IOException {
        if (!hasPackManifest(target)) {
            throw new IOException("Style pack has no style.json or pack.json");
        }

        Set<DollResourceId> ids = new HashSet<>();
        try (var files = Files.walk(target)) {
            for (Path styleFile : files
                    .filter(path -> path.getFileName().toString().equals("style.json"))
                    .toList()) {
                JsonObject style = read(styleFile);
                if (style == null || !style.has("id")) continue;
                DollResourceId id = DollResourceId.tryParse(style.get("id").getAsString());
                if (id == null) throw new IOException("Invalid style id in " + styleFile.getFileName());
                if (!ids.add(id) || DollStyles.contains(id) || importedStyleExists(id, target)) {
                    throw new IOException("Duplicate style id: " + id);
                }
            }
        }
    }

    private static boolean importedStyleExists(DollResourceId id, Path currentPack) throws IOException {
        Path imported = stylesDirectory.resolve("imported");
        if (!Files.isDirectory(imported)) return false;
        try (var packs = Files.list(imported)) {
            for (Path pack : packs.filter(Files::isDirectory).toList()) {
                if (pack.equals(currentPack)) continue;
                try (var files = Files.walk(pack)) {
                    for (Path styleFile : files
                            .filter(path -> path.getFileName().toString().equals("style.json"))
                            .toList()) {
                        JsonObject style = read(styleFile);
                        if (style != null && style.has("id")
                                && id.equals(DollResourceId.tryParse(style.get("id").getAsString()))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    private static JsonObject read(Path path) throws IOException { return GSON.fromJson(Files.readString(path), JsonObject.class); }
    private static void write(Path path, JsonObject value) throws IOException { Files.createDirectories(path.getParent()); Files.writeString(path, GSON.toJson(value)); }
    private static String safePath(JsonObject object, String key) throws IOException { if (!object.has(key)) throw new IOException("Missing " + key); return object.get(key).getAsString(); }
    private static Path safeResolve(Path root, String value) throws IOException { Path path = root.resolve(value.replace('\\', '/')).normalize(); if (value.startsWith("/") || value.contains("..") || !path.startsWith(root.normalize())) throw new IOException("Invalid relative path"); return path; }
    private static void addEntry(ZipOutputStream zip, String name, byte[] data) throws IOException { zip.putNextEntry(new ZipEntry(name)); zip.write(data); zip.closeEntry(); }
    private static String uniqueKey(String value) { String base = safeName(value); Path path = stylesDirectory.resolve("imported").resolve(base); return Files.exists(path) ? base + "_" + UUID.randomUUID().toString().substring(0, 8) : base; }
    private static String safeName(String value) { String result = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "_"); return result.isBlank() ? "style" : result; }
    private static void copyTree(Path source, Path target) throws IOException { try (var files = Files.walk(source)) { for (Path file : files.toList()) { Path destination = target.resolve(source.relativize(file).toString()); if (Files.isDirectory(file)) Files.createDirectories(destination); else { Files.createDirectories(destination.getParent()); Files.copy(file, destination); } } } }
    private static void deleteTree(Path root) throws IOException { if (Files.exists(root)) try (var files = Files.walk(root)) { for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(file); } }
    private StylePackStore() {}
}
