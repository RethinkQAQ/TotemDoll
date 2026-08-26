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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rethinkqaq.totemdoll.Constants;
import com.rethinkqaq.totemdoll.platform.Services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DollDisplayOverrideResolver {

    public static List<DollDisplayOverride> read(JsonObject style) {
        JsonElement compatibilityElement = style.get("compatibility");
        if (compatibilityElement == null || !compatibilityElement.isJsonObject()) {
            if (compatibilityElement != null) {
                Constants.LOG.warn("Ignoring invalid style compatibility declaration");
            }
            return List.of();
        }
        JsonObject compatibility = compatibilityElement.getAsJsonObject();
        if (compatibility == null) return List.of();
        JsonElement declarationsElement = compatibility.get("display_overrides");
        if (declarationsElement == null || !declarationsElement.isJsonArray()) {
            if (declarationsElement != null) {
                Constants.LOG.warn("Ignoring invalid style display_overrides declaration");
            }
            return List.of();
        }
        JsonArray declarations = declarationsElement.getAsJsonArray();
        if (declarations == null) return List.of();

        List<DollDisplayOverride> result = new ArrayList<>();
        for (JsonElement element : declarations) {
            try {
                JsonObject declaration = element.getAsJsonObject();
                JsonArray modArray = declaration.getAsJsonArray("mods");
                if (modArray == null || modArray.isEmpty()) {
                    throw new IllegalArgumentException("mods must not be empty");
                }
                String match = declaration.has("match")
                        ? declaration.get("match").getAsString().toLowerCase(Locale.ROOT) : "any";
                if (!"any".equals(match)) {
                    throw new IllegalArgumentException("unsupported match: " + match);
                }
                String perspective = declaration.has("perspective")
                        ? declaration.get("perspective").getAsString().toLowerCase(Locale.ROOT) : "";
                if (!perspective.isEmpty() && !"firstperson".equals(perspective)) {
                    throw new IllegalArgumentException("unsupported perspective: " + perspective);
                }

                List<String> mods = new ArrayList<>();
                for (JsonElement mod : modArray) {
                    String id = mod.getAsString().trim().toLowerCase(Locale.ROOT);
                    if (!id.isEmpty() && !mods.contains(id)) mods.add(id);
                }
                if (mods.isEmpty()) throw new IllegalArgumentException("mods must not be empty");

                JsonObject contexts = declaration.getAsJsonObject("contexts");
                if (contexts == null) throw new IllegalArgumentException("contexts is missing");
                Map<String, DollDisplayTransformPatch> patches = new LinkedHashMap<>();
                for (String context : contexts.keySet()) {
                    if (!DollDisplayContext.isSupported(context)) {
                        Constants.LOG.warn("Ignoring unsupported display override context {}", context);
                        continue;
                    }
                    patches.put(context, readPatch(contexts.getAsJsonObject(context)));
                }
                if (patches.isEmpty()) throw new IllegalArgumentException("contexts is empty");
                result.add(new DollDisplayOverride(List.copyOf(mods), "firstperson".equals(perspective),
                        Map.copyOf(patches)));
            } catch (Exception exception) {
                Constants.LOG.warn("Ignoring invalid display compatibility override: {}", exception.getMessage());
                Constants.LOG.debug("Display compatibility override parsing failure", exception);
            }
        }
        return List.copyOf(result);
    }

    public static DollDisplayProfiles resolveProfiles(
            Map<String, DollDisplayTransform> base,
            List<DollDisplayOverride> overrides,
            Map<String, Boolean> loadedModCache
    ) {
        if (overrides.isEmpty()) return new DollDisplayProfiles(base, base);
        Set<String> loadedMods = new HashSet<>();
        for (DollDisplayOverride override : overrides) {
            for (String mod : override.mods()) {
                if (loadedModCache.computeIfAbsent(mod, id -> Services.PLATFORM.isModLoaded(id))) {
                    loadedMods.add(mod);
                }
            }
        }

        Map<String, DollDisplayTransform> normal = new LinkedHashMap<>(base);
        Map<String, DollDisplayTransform> firstPerson = new LinkedHashMap<>(base);
        for (String context : DollDisplayContext.supported()) {
            DollDisplayTransform original = DollDisplayContext.resolve(base, context);
            if (original == null) continue;
            DollDisplayTransform normalDisplay = original;
            DollDisplayTransform firstPersonDisplay = original;
            for (DollDisplayOverride override : overrides) {
                if (!override.matches(loadedMods)) continue;
                DollDisplayTransformPatch patch = override.contexts().get(context);
                if (patch == null) continue;
                if (!override.firstPersonOnly()) normalDisplay = patch.apply(normalDisplay);
                firstPersonDisplay = patch.apply(firstPersonDisplay);
            }
            if (!normalDisplay.equals(original) || base.containsKey(context)) {
                normal.put(context, normalDisplay);
            }
            if (!firstPersonDisplay.equals(original) || base.containsKey(context)) {
                firstPerson.put(context, firstPersonDisplay);
            }
        }
        return new DollDisplayProfiles(normal, firstPerson);
    }

    private static DollDisplayTransformPatch readPatch(JsonObject object) {
        return new DollDisplayTransformPatch(
                vector(object, "rotation"),
                vector(object, "translation", true),
                vector(object, "scale")
        );
    }

    private static DollDisplayVector vector(JsonObject object, String key) {
        return vector(object, key, false);
    }

    private static DollDisplayVector vector(JsonObject object, String key, boolean translation) {
        if (!object.has(key)) return null;
        JsonArray array = object.getAsJsonArray(key);
        if (array == null || array.size() != 3) throw new IllegalArgumentException(key + " must have 3 values");
        float[] result = new float[3];
        for (int index = 0; index < 3; index++) {
            result[index] = array.get(index).getAsFloat();
            if (!Float.isFinite(result[index])) throw new IllegalArgumentException(key + " contains a non-finite value");
            if (translation) result[index] /= 16F;
        }
        return new DollDisplayVector(result[0], result[1], result[2]);
    }

    private DollDisplayOverrideResolver() {}
}
