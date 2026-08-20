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
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModel;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModelLoader;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneActionManager;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;
import com.rethinkqaq.totemdoll.client.gui.DollGuiPreviewRenderer;

import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Loads version-independent format-3 mesh style manifests. */
public final class DollStyleLoader {
    private static final Gson GSON = new Gson();

    public static List<DollStyle> reload(ResourceManager manager) {
        DollBoneRenderer.clear();
        DollGuiPreviewRenderer.invalidateAll();
        DollBoneActionManager.clear();
        DollBoneModels.clear();
        List<DollStyle> styles = new ArrayList<>();
        List<Map.Entry<DollResourceId, Resource>> resources = new ArrayList<>();
        for (var entry : manager.listResources("styles", id -> id.getPath().endsWith("/style.json")).entrySet()) {
            resources.add(Map.entry(DollMinecraftResourceUtil.fromNative(entry.getKey()), entry.getValue()));
        }
        resources.sort(Map.Entry.comparingByKey(Comparator.comparing(DollResourceId::toString)));
        resources.forEach(entry -> loadOne(manager, entry.getKey(), entry.getValue(), styles));
        DollStyles.replaceDiscovered(styles);
        return List.copyOf(styles);
    }

    private static void loadOne(ResourceManager manager, DollResourceId source, Resource resource,
                                List<DollStyle> output) {
        try (Reader reader = resource.openAsReader()) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("format") || root.get("format").getAsInt() != 3) {
                throw new IllegalArgumentException("Expected style format 3");
            }
            if (root.has("enabled") && !root.get("enabled").getAsBoolean()) {
                return;
            }
            DollResourceId id = requiredId(root, "id");
            DollStyleOrigin origin = readOrigin(root, source);
            DollStylePackMetadata packMetadata = origin == DollStyleOrigin.IMPORTED
                    ? readPackMetadata(manager, source) : null;
            if (root.has("invalid") && root.get("invalid").getAsBoolean()) {
                String name = root.has("name") ? root.get("name").getAsString() : id.toString();
                String reason = root.has("invalid_reason") ? root.get("invalid_reason").getAsString() : null;
                output.add(new DollStyle(id, name, null, DollResourceId.ofVanilla("item/totem_of_undying"),
                        false, null, false, null, origin, Map.of(), List.of(), "invalid", source,
                        packMetadata, reason));
                return;
            }
            JsonObject model = root.getAsJsonObject("model");
            if (model == null || !model.has("type")) throw new IllegalArgumentException("Missing model.type");
            String modelType = model.get("type").getAsString();
            if (!"mesh".equals(modelType)) throw new IllegalArgumentException("Unsupported model type " + modelType);
            DollBoneModel loadedModel = DollBoneModelLoader.load(manager, source, root, model);
            DollBoneModels.put(id, loadedModel);
            DollResourceId modelId = DollResourceId.ofVanilla("item/totem_of_undying");
            String name = root.has("name") ? root.get("name").getAsString() : id.toString();
            String nameKey = root.has("name_key") ? root.get("name_key").getAsString() : null;
            DollResourceId template = root.has("template") ? requiredId(root, "template") : null;
            DollSkinDefinition skin = readSkin(root);
            Map<String, DollResourceId> textures = readTextures(source, root);
            List<DollAnimationDefinition> animations = readTextureAnimations(root, textures);
            output.add(new DollStyle(id, name, nameKey, modelId, false, template,
                    origin == DollStyleOrigin.LOCAL, skin, origin, textures, animations,
                    modelType, source, packMetadata, null));
        } catch (Exception exception) {
            Constants.LOG.error("Could not load style {}", source, exception);
        }
    }

    private static DollStyleOrigin readOrigin(JsonObject root, DollResourceId source) {
        if (root.has("origin")) {
            try { return DollStyleOrigin.valueOf(root.get("origin").getAsString().toUpperCase()); }
            catch (IllegalArgumentException ignored) { }
        }
        return Constants.MOD_ID.equals(source.namespace()) ? DollStyleOrigin.BUILTIN : DollStyleOrigin.RESOURCE_PACK;
    }

    private static DollStylePackMetadata readPackMetadata(ResourceManager manager, DollResourceId source) {
        try {
            DollResourceId location = resolve(source, "pack_metadata.json");
            try (Reader reader = openPackMetadata(manager, location)) {
                JsonObject object = GSON.fromJson(reader, JsonObject.class);
                return new DollStylePackMetadata(
                        stringOrNull(object, "id"), stringOrNull(object, "name"), stringOrNull(object, "author"),
                        stringOrNull(object, "license_name"), stringOrNull(object, "license_summary"),
                        stringOrNull(object, "readme_name"), stringOrNull(object, "readme_summary"),
                        stringOrNull(object, "storage_key"));
            }
        } catch (Exception exception) {
            Constants.LOG.warn("Could not load imported style pack metadata for {}", source, exception);
            return null;
        }
    }

    private static Reader openPackMetadata(ResourceManager manager, DollResourceId location) throws IOException {
        return manager.getResource(DollMinecraftResourceUtil.nativeId(location))
                .orElseThrow(() -> new IOException("Missing pack metadata"))
                .openAsReader();
    }

    private static String stringOrNull(JsonObject object, String key) {
        return object != null && object.has(key) ? object.get(key).getAsString() : null;
    }

    private static DollSkinDefinition readSkin(JsonObject root) {
        JsonObject skin = root.getAsJsonObject("skin");
        if (skin == null || !skin.has("supported") || !skin.get("supported").getAsBoolean()
                || !skin.has("format") || !skin.has("target")) return null;
        DollSkinDefinition result = new DollSkinDefinition(skin.get("format").getAsString(), skin.get("target").getAsString());
        return result.supportsImport() ? result : null;
    }

    private static Map<String, DollResourceId> readTextures(DollResourceId source, JsonObject root) {
        Map<String, DollResourceId> result = new LinkedHashMap<>();
        JsonObject textures = root.getAsJsonObject("textures");
        if (textures == null) return result;
        for (String key : textures.keySet()) {
            String path = safeRelativePath(textures.get(key).getAsString());
            result.put(key, resolve(source, path));
        }
        return result;
    }

    private static List<DollAnimationDefinition> readAnimations(JsonObject root, Map<String, DollResourceId> textures) {
        List<DollAnimationDefinition> result = new ArrayList<>();
        JsonObject animations = root.getAsJsonObject("animations");
        if (animations == null) return result;
        for (String id : animations.keySet()) {
            try {
                JsonObject object = animations.getAsJsonObject(id);
                if (!"frame_sequence".equals(object.get("type").getAsString())) continue;
                JsonArray frameArray = object.getAsJsonArray("frames");
                if (frameArray == null || frameArray.isEmpty() || frameArray.size() > 64) continue;
                List<String> frames = new ArrayList<>();
                for (var frame : frameArray) {
                    String name = frame.getAsString();
                    if (!textures.containsKey(name)) throw new IllegalArgumentException("Unknown texture frame " + name);
                    frames.add(name);
                }
                int duration = object.get("frame_duration").getAsInt();
                JsonObject interval = object.getAsJsonObject("interval");
                int min = interval == null ? 80 : interval.get("min").getAsInt();
                int max = interval == null ? 180 : interval.get("max").getAsInt();
                String trigger = object.get("trigger").getAsString();
                boolean supportedTrigger = "random_idle".equals(trigger)
                        || "loop".equals(trigger)
                        || "on_screen_open".equals(trigger)
                        || "on_totem_activate".equals(trigger)
                        || "manual".equals(trigger)
                        || "linked".equals(trigger);
                if (duration <= 0 || min < 0 || max < min || !supportedTrigger) continue;
                result.add(new DollAnimationDefinition(id, List.copyOf(frames), duration, trigger, min, max));
            } catch (Exception ignored) { }
        }
        return result;
    }

    private static List<DollAnimationDefinition> readTextureAnimations(JsonObject root,
                                                                         Map<String, DollResourceId> textures) {
        if (!root.has("texture_animations")) return List.of();
        JsonObject copy = root.deepCopy();
        copy.add("animations", root.get("texture_animations").deepCopy());
        return readAnimations(copy, textures);
    }

    private static DollResourceId resolve(DollResourceId source, String relative) {
        String parent = source.path().substring(0, source.path().lastIndexOf('/') + 1);
        return DollResourceId.of(source.namespace(), parent + relative);
    }

    private static String safeRelativePath(String value) {
        String path = value.replace('\\', '/');
        if (path.isBlank() || path.startsWith("/") || path.contains("..") || path.contains(":"))
            throw new IllegalArgumentException("Invalid relative path " + value);
        if (!path.endsWith(".png")) throw new IllegalArgumentException("Texture must be a PNG: " + value);
        return path;
    }

    private static DollResourceId requiredId(JsonObject object, String key) {
        if (!object.has(key)) throw new IllegalArgumentException("Missing " + key);
        DollResourceId id = DollResourceId.tryParse(object.get(key).getAsString());
        if (id == null) throw new IllegalArgumentException("Invalid resource location: " + key);
        return id;
    }

    private static String requiredPath(JsonObject object, String key) {
        if (!object.has(key)) throw new IllegalArgumentException("Missing " + key);
        String path = object.get(key).getAsString();
        if (path.isBlank() || path.startsWith("/") || path.contains("..")) throw new IllegalArgumentException("Invalid relative path");
        return path;
    }

    private DollStyleLoader() {}
}
