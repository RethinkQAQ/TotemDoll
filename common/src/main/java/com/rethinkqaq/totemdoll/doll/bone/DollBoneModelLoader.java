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

package com.rethinkqaq.totemdoll.doll.bone;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DollBoneModelLoader {
    private static final Gson GSON = new Gson();

    public static DollBoneModel load(ResourceManager manager, DollResourceId styleSource,
                                     JsonObject style, JsonObject model) throws IOException {
        String geometryPath = safePath(model, "geometry");
        String animationsPath = model.has("animations") ? safePath(model, "animations") : null;
        JsonObject geometry = read(manager, resolve(styleSource, geometryPath));
        if (!geometry.has("format") || geometry.get("format").getAsInt() != 1)
            throw new IOException("Expected bone geometry format 1");
        int textureWidth = integer(geometry, "texture_width", 64);
        int textureHeight = integer(geometry, "texture_height", 64);
        JsonObject textures = style.getAsJsonObject("textures");
        if (textures == null || !textures.has("base")) throw new IOException("Mesh model requires textures.base");
        String texturePath = safeRelativePath(textures.get("base").getAsString(), "textures.base");
        DollResourceId texture = resolve(styleSource, texturePath);

        List<DollBone> roots = new ArrayList<>();
        List<JsonElement> bones = elements(geometry, "bones");
        if (bones.isEmpty()) throw new IOException("Bone model has no bones");
        Counter counter = new Counter();
        for (JsonElement element : bones) roots.add(readBone(element.getAsJsonObject(), 0, counter));

        Map<String, DollBoneAnimation> animations = animationsPath == null
                ? Map.of() : readAnimations(manager, resolve(styleSource, animationsPath));
        List<DollActionBinding> bindings = readBindings(style, animations);
        return new DollBoneModel(textureWidth, textureHeight, texture, List.copyOf(roots),
                Map.copyOf(animations), List.copyOf(bindings), readDisplay(style, geometry));
    }

    private static Map<String, DollDisplayTransform> readDisplay(JsonObject style, JsonObject geometry) {
        JsonObject display = style.getAsJsonObject("display");
        if (display == null) display = geometry.getAsJsonObject("display");
        if (display == null) return Map.of();
        Map<String, DollDisplayTransform> result = new LinkedHashMap<>();
        for (String context : display.keySet()) {
            JsonObject transform = display.getAsJsonObject(context);
            if (transform == null) continue;
            float[] rotation = vector(transform.getAsJsonArray("rotation"), 0, 0, 0);
            float[] translation = vector(transform.getAsJsonArray("translation"), 0, 0, 0);
            float[] scale = vector(transform.getAsJsonArray("scale"), 1, 1, 1);
            // Java item JSON stores display translations in model units.
            // ItemTransform's public constructor expects block-space values,
            // because its JSON deserializer normally performs this conversion.
            translation[0] /= 16F;
            translation[1] /= 16F;
            translation[2] /= 16F;
            result.put(normalizeDisplayContext(context), new DollDisplayTransform(rotation[0], rotation[1], rotation[2],
                    translation[0], translation[1], translation[2], scale[0], scale[1], scale[2]));
        }
        return Map.copyOf(result);
    }

    private static String normalizeDisplayContext(String context) {
        return switch (context) {
            case "firstperson_righthand", "firstperson_lefthand" -> "firstperson";
            case "thirdperson_righthand", "thirdperson_lefthand" -> "thirdperson";
            case "none" -> "fixed";
            default -> context;
        };
    }

    private static DollBone readBone(JsonObject object, int depth, Counter counter) throws IOException {
        if (depth > 64 || ++counter.value > 256) throw new IOException("Bone model is too complex");
        String name = object.has("name") ? object.get("name").getAsString() : "";
        if (name.isBlank()) throw new IOException("Bone name cannot be empty");
        float[] pivot = vector(object.getAsJsonArray("pivot"), 0, 0, 0);
        float[] rotation = vector(object.getAsJsonArray("rotation"), 0, 0, 0);
        List<DollCube> cubes = new ArrayList<>();
        for (JsonElement element : elements(object, "cubes")) cubes.add(readCube(element.getAsJsonObject()));
        List<DollBone> children = new ArrayList<>();
        for (JsonElement element : elements(object, "children"))
            children.add(readBone(element.getAsJsonObject(), depth + 1, counter));
        return new DollBone(name, pivot[0], pivot[1], pivot[2], rotation[0], rotation[1], rotation[2],
                List.copyOf(cubes), List.copyOf(children));
    }

    private static DollCube readCube(JsonObject object) throws IOException {
        float[] origin = vector(object.getAsJsonArray("origin"), 0, 0, 0);
        float[] size = vector(object.getAsJsonArray("size"), 0, 0, 0);
        if (size[0] < 0 || size[1] < 0 || size[2] < 0) throw new IOException("Cube size cannot be negative");
        float[] uv = vector(object.getAsJsonArray("uv"), 0, 0, 0);
        Map<String, DollFace> faces = new LinkedHashMap<>();
        JsonObject faceObject = object.getAsJsonObject("faces");
        if (faceObject != null) for (String direction : faceObject.keySet()) {
            JsonObject face = faceObject.getAsJsonObject(direction);
            JsonArray faceUv = face == null ? null : face.getAsJsonArray("uv");
            if (faceUv == null || faceUv.size() < 4) continue;
            int rotation = face.has("rotation") ? face.get("rotation").getAsInt() : 0;
            rotation = Math.floorMod(rotation, 360);
            if (rotation % 90 != 0) continue;
            faces.put(direction, new DollFace(faceUv.get(0).getAsFloat(), faceUv.get(1).getAsFloat(),
                    faceUv.get(2).getAsFloat(), faceUv.get(3).getAsFloat(), rotation));
        }
        return new DollCube(origin[0], origin[1], origin[2], size[0], size[1], size[2],
                (int) uv[0], (int) uv[1], object.has("mirror") && object.get("mirror").getAsBoolean(),
                Map.copyOf(faces));
    }

    private static Map<String, DollBoneAnimation> readAnimations(ResourceManager manager,
                                                                 DollResourceId location) throws IOException {
        JsonObject root = read(manager, location);
        if (!root.has("format") || root.get("format").getAsInt() != 1) throw new IOException("Expected animation format 1");
        Map<String, DollBoneAnimation> result = new LinkedHashMap<>();
        JsonObject animations = root.getAsJsonObject("animations");
        if (animations == null) return result;
        if (animations.size() > 64) throw new IOException("Too many animations");
        for (String id : animations.keySet()) {
            JsonObject animation = animations.getAsJsonObject(id);
            int length = integer(animation, "length", 1);
            if (length <= 0) continue;
            Map<String, DollBoneTimeline> timelines = new LinkedHashMap<>();
            JsonObject bones = animation.getAsJsonObject("bones");
            if (bones != null) for (String bone : bones.keySet()) {
                JsonObject timeline = bones.getAsJsonObject(bone);
                timelines.put(bone, new DollBoneTimeline(readFrames(timeline, "rotation"),
                        readFrames(timeline, "position"), readFrames(timeline, "scale")));
            }
            result.put(id, new DollBoneAnimation(id,
                    animation.has("loop") && animation.get("loop").getAsBoolean(), length, Map.copyOf(timelines)));
        }
        return result;
    }

    private static List<DollKeyframe> readFrames(JsonObject timeline, String key) {
        List<DollKeyframe> result = new ArrayList<>();
        for (JsonElement element : elements(timeline, key)) {
            JsonObject frame = element.getAsJsonObject();
            float[] value = vector(frame.getAsJsonArray("value"), 0, 0, 0);
            result.add(new DollKeyframe(frame.get("time").getAsFloat(), value[0], value[1], value[2],
                    frame.has("interpolation") ? frame.get("interpolation").getAsString() : "linear"));
        }
        result.sort(Comparator.comparingDouble(DollKeyframe::time));
        return List.copyOf(result);
    }

    /**
     * Blockbench exporters and JSON writers sometimes serialize a one-item
     * collection as an object. Accept both forms so valid single-bone and
     * single-keyframe files remain loadable.
     */
    private static List<JsonElement> elements(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) return List.of();
        JsonElement value = object.get(key);
        if (value.isJsonArray()) {
            List<JsonElement> result = new ArrayList<>();
            value.getAsJsonArray().forEach(result::add);
            return result;
        }
        return List.of(value);
    }

    private static List<DollActionBinding> readBindings(JsonObject style,
                                                         Map<String, DollBoneAnimation> animations) {
        List<DollActionBinding> result = new ArrayList<>();
        JsonObject declarations = style.getAsJsonObject("animations");
        if (declarations == null) return result;
        for (String id : declarations.keySet()) {
            JsonObject declaration = declarations.getAsJsonObject(id);
            if (!declaration.has("animation")) continue;
            String animation = declaration.get("animation").getAsString();
            if (!animations.containsKey(animation)) continue;
            String trigger = declaration.has("trigger") ? declaration.get("trigger").getAsString() : "manual";
            int priority = declaration.has("priority") ? declaration.get("priority").getAsInt() : defaultPriority(trigger);
            JsonObject interval = declaration.getAsJsonObject("interval");
            int min = interval == null ? 80 : integer(interval, "min", 80);
            int max = interval == null ? 180 : integer(interval, "max", 180);
            result.add(new DollActionBinding(id, animation, trigger, priority, min, Math.max(min, max)));
        }
        return result;
    }

    private static int defaultPriority(String trigger) {
        return switch (trigger) {
            case "on_totem_activate" -> 100;
            case "manual" -> 80;
            case "on_screen_open" -> 60;
            default -> 20;
        };
    }

    private static JsonObject read(ResourceManager manager, DollResourceId location) throws IOException {
        try (Reader reader = manager.getResource(DollMinecraftResourceUtil.nativeId(location))
                .orElseThrow(() -> new IOException("Missing " + location)).openAsReader()) {
            JsonObject value = GSON.fromJson(reader, JsonObject.class);
            if (value == null) throw new IOException("Empty " + location);
            return value;
        }
    }

    private static DollResourceId resolve(DollResourceId source, String relative) {
        String parent = source.path().substring(0, source.path().lastIndexOf('/') + 1);
        return DollResourceId.of(source.namespace(), parent + relative);
    }

    private static String safePath(JsonObject object, String key) throws IOException {
        if (!object.has(key)) throw new IOException("Missing model." + key);
        String value = object.get(key).getAsString().replace('\\', '/');
        if (value.isBlank() || value.startsWith("/") || value.contains("..")) throw new IOException("Invalid path " + value);
        return value;
    }

    private static String safeRelativePath(String value, String key) throws IOException {
        String path = value.replace('\\', '/');
        if (path.isBlank() || path.startsWith("/") || path.contains("..") || path.contains(":"))
            throw new IOException("Invalid relative path " + key);
        if (!path.endsWith(".png")) throw new IOException(key + " must reference a PNG");
        return path;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static float[] vector(JsonArray array, float x, float y, float z) {
        if (array == null || array.size() < 3) return new float[]{x, y, z};
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    private static final class Counter { private int value; }
    private DollBoneModelLoader() {}
}
