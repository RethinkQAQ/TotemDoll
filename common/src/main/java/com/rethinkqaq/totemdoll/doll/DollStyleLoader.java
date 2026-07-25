package com.rethinkqaq.totemdoll.doll;

import com.rethinkqaq.totemdoll.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModel;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModelLoader;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneModels;
import com.rethinkqaq.totemdoll.doll.bone.DollBoneActionManager;
import com.rethinkqaq.totemdoll.client.DollBoneRenderer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Loads the format-2 style manifests supplied by built-in resources and packs. */
public final class DollStyleLoader {
    private static final Gson GSON = new Gson();

    public static List<DollStyle> reload(ResourceManager manager) {
        DollBoneRenderer.clear();
        DollBoneActionManager.clear();
        DollBoneModels.clear();
        Map<ResourceLocation, Resource> resources = manager.listResources(
                "styles", id -> id.getPath().endsWith("/style.json"));
        List<DollStyle> styles = new ArrayList<>();
        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> loadOne(manager, entry.getKey(), entry.getValue(), styles));
        DollStyles.replaceDiscovered(styles);
        return List.copyOf(styles);
    }

    private static void loadOne(ResourceManager manager, ResourceLocation source, Resource resource,
                                List<DollStyle> output) {
        try (Reader reader = resource.openAsReader()) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("format") || root.get("format").getAsInt() != 2) {
                throw new IllegalArgumentException("Expected style format 2");
            }
            if (root.has("enabled") && !root.get("enabled").getAsBoolean()) {
                return;
            }
            ResourceLocation id = requiredId(root, "id");
            JsonObject model = root.getAsJsonObject("model");
            if (model == null || !model.has("type")) throw new IllegalArgumentException("Missing model.type");
            String modelType = model.get("type").getAsString();
            boolean boneModel = "minecraft_bone".equals(modelType);
            if (!boneModel && !"minecraft_item".equals(modelType))
                throw new IllegalArgumentException("Unsupported model type " + modelType);
            ResourceLocation modelId;
            if (boneModel) {
                DollBoneModel loadedModel = DollBoneModelLoader.load(manager, source, root, model);
                DollBoneModels.put(id, loadedModel);
                modelId = ResourceLocation.withDefaultNamespace("item/totem_of_undying");
            } else {
                modelId = resolveAsset(source, requiredPath(model, "file"));
            }
            String name = root.has("name") ? root.get("name").getAsString() : id.toString();
            String nameKey = root.has("name_key") ? root.get("name_key").getAsString() : null;
            ResourceLocation template = root.has("template") ? requiredId(root, "template") : null;
            DollSkinDefinition skin = readSkin(root);
            DollStyleOrigin origin = readOrigin(root, source);
            Map<String, ResourceLocation> textures = readTextures(root);
            List<DollAnimationDefinition> animations = boneModel ? List.of() : readAnimations(root, textures);
            output.add(new DollStyle(id, name, nameKey, modelId, !boneModel, template,
                    origin == DollStyleOrigin.LOCAL, skin, origin, textures, animations,
                    modelType, source));
        } catch (Exception exception) {
            Constants.LOG.error("Could not load style {}", source, exception);
        }
    }

    private static ResourceLocation resolveAsset(ResourceLocation source, String file) {
        String path = file.replace('\\', '/');
        if (path.startsWith("models/")) {
            return ResourceLocation.fromNamespaceAndPath(source.getNamespace(), path.substring(7, path.length() - (path.endsWith(".json") ? 5 : 0)));
        }
        String parent = source.getPath().substring(0, source.getPath().lastIndexOf('/'));
        String resolved = parent + "/" + path;
        if (resolved.startsWith("styles/")) resolved = resolved.substring(7);
        return ResourceLocation.fromNamespaceAndPath(source.getNamespace(), resolved.substring(0, resolved.length() - 5));
    }

    private static DollStyleOrigin readOrigin(JsonObject root, ResourceLocation source) {
        if (root.has("origin")) {
            try { return DollStyleOrigin.valueOf(root.get("origin").getAsString().toUpperCase()); }
            catch (IllegalArgumentException ignored) { }
        }
        return Constants.MOD_ID.equals(source.getNamespace()) ? DollStyleOrigin.BUILTIN : DollStyleOrigin.RESOURCE_PACK;
    }

    private static DollSkinDefinition readSkin(JsonObject root) {
        JsonObject skin = root.getAsJsonObject("skin");
        if (skin == null || !skin.has("supported") || !skin.get("supported").getAsBoolean()
                || !skin.has("format") || !skin.has("target")) return null;
        DollSkinDefinition result = new DollSkinDefinition(skin.get("format").getAsString(), skin.get("target").getAsString());
        return result.supportsImport() ? result : null;
    }

    private static Map<String, ResourceLocation> readTextures(JsonObject root) {
        Map<String, ResourceLocation> result = new LinkedHashMap<>();
        JsonObject textures = root.getAsJsonObject("textures");
        if (textures == null) return result;
        for (String key : textures.keySet()) {
            ResourceLocation id = ResourceLocation.tryParse(textures.get(key).getAsString());
            if (id != null) result.put(key, id);
        }
        return result;
    }

    private static List<DollAnimationDefinition> readAnimations(JsonObject root, Map<String, ResourceLocation> textures) {
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
                        || "manual".equals(trigger);
                if (duration <= 0 || min < 0 || max < min || !supportedTrigger) continue;
                result.add(new DollAnimationDefinition(id, List.copyOf(frames), duration, trigger, min, max));
            } catch (Exception ignored) { }
        }
        return result;
    }

    private static ResourceLocation requiredId(JsonObject object, String key) {
        if (!object.has(key)) throw new IllegalArgumentException("Missing " + key);
        ResourceLocation id = ResourceLocation.tryParse(object.get(key).getAsString());
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
