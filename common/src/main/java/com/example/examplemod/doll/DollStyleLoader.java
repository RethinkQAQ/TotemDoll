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

public final class DollStyleLoader {

    private static final Gson GSON = new Gson();

    public static List<DollStyle> reload(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                "dolls",
                id -> id.getPath().endsWith("/doll.json")
        );
        List<DollStyle> loaded = new ArrayList<>();

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> loadOne(entry.getKey(), entry.getValue(), loaded));

        DollStyles.replaceDiscovered(loaded);
        return List.copyOf(loaded);
    }

    private static void loadOne(
            ResourceLocation source,
            Resource resource,
            List<DollStyle> destination
    ) {
        try (Reader reader = resource.openAsReader()) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || root.get("format").getAsInt() != 1) {
                throw new IllegalArgumentException("Unsupported or missing format");
            }

            ResourceLocation id = requireId(root, "id");
            JsonObject modelObject = root.getAsJsonObject("model");
            if (modelObject == null
                    || !"minecraft_item".equals(modelObject.get("type").getAsString())) {
                throw new IllegalArgumentException("Only minecraft_item models are supported");
            }

            ResourceLocation declaredModel = requireId(modelObject, "path");
            String modelPath = declaredModel.getPath();
            if (modelPath.startsWith("models/")) {
                modelPath = modelPath.substring("models/".length());
            }

            String name = root.has("name") ? root.get("name").getAsString() : id.toString();
            String nameKey = root.has("name_key") ? root.get("name_key").getAsString() : null;
            ResourceLocation templateId = root.has("template")
                    ? requireId(root, "template")
                    : null;
            boolean userCreated = root.has("user_created") && root.get("user_created").getAsBoolean();
            DollSkinDefinition skin = readSkin(root);
            destination.add(new DollStyle(
                    id,
                    name,
                    nameKey,
                    ResourceLocation.fromNamespaceAndPath(declaredModel.getNamespace(), modelPath),
                    true,
                    templateId,
                    userCreated,
                    skin
            ));
        } catch (Exception exception) {
            Constants.LOG.error("Could not load doll style {}", source, exception);
        }
    }

    private static DollSkinDefinition readSkin(JsonObject root) {
        JsonObject skin = root.getAsJsonObject("skin");
        if (skin == null || !skin.has("format") || !skin.has("texture_slot")) {
            return null;
        }
        DollSkinDefinition definition = new DollSkinDefinition(
                skin.get("format").getAsString(),
                skin.get("texture_slot").getAsString()
        );
        return definition.supportsImport() ? definition : null;
    }

    private static ResourceLocation requireId(JsonObject object, String member) {
        if (!object.has(member)) {
            throw new IllegalArgumentException("Missing " + member);
        }
        ResourceLocation id = ResourceLocation.tryParse(object.get(member).getAsString());
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource location in " + member);
        }
        return id;
    }

    private DollStyleLoader() {
    }
}
