package com.example.examplemod.doll;

import com.example.examplemod.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Loads the format-2 style manifests supplied by built-in resources and packs. */
public final class DollStyleLoader {
    private static final Gson GSON = new Gson();

    public static List<DollStyle> reload(ResourceManager manager) {
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
            ResourceLocation id = requiredId(root, "id");
            JsonObject model = root.getAsJsonObject("model");
            if (model == null || !"minecraft_item".equals(model.get("type").getAsString())) {
                throw new IllegalArgumentException("Only minecraft_item models are supported");
            }
            String file = requiredPath(model, "file");
            ResourceLocation modelId = resolveAsset(source, file);
            String name = root.has("name") ? root.get("name").getAsString() : id.toString();
            String nameKey = root.has("name_key") ? root.get("name_key").getAsString() : null;
            ResourceLocation template = root.has("template") ? requiredId(root, "template") : null;
            DollSkinDefinition skin = readSkin(root);
            DollStyleOrigin origin = readOrigin(root, source);
            output.add(new DollStyle(id, name, nameKey, modelId, true, template,
                    origin == DollStyleOrigin.LOCAL, skin, origin));
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
