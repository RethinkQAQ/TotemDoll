package com.example.examplemod.doll;

import com.example.examplemod.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
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
            rebuildGeneratedPack();
        } catch (IOException exception) {
            Constants.LOG.error("Could not initialize local doll styles", exception);
        }
    }

    public static ResourceLocation importSkin(
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

        String key = "user_" + UUID.randomUUID().toString().replace("-", "");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key);
        ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID,
                "generated/" + key
        );
        ResourceLocation textureId = ResourceLocation.fromNamespaceAndPath(
                Constants.MOD_ID,
                "item/generated/" + key + "/skin"
        );

        JsonObject model = readTemplateModel(template, resourceManager);
        replaceTexture(model, template.skin().textureSlot(), textureId.toString());

        JsonObject style = new JsonObject();
        style.addProperty("format", 1);
        style.addProperty("id", id.toString());
        style.addProperty("name", normalizeName(displayName, sourceSkin));
        style.addProperty("template", template.id().toString());
        style.addProperty("user_created", true);

        JsonObject modelDeclaration = new JsonObject();
        modelDeclaration.addProperty("type", "minecraft_item");
        modelDeclaration.addProperty("path", Constants.MOD_ID + ":models/" + modelId.getPath());
        style.add("model", modelDeclaration);

        JsonObject textures = new JsonObject();
        textures.addProperty("base", textureId.toString());
        style.add("textures", textures);

        Path styleDirectory = stylesDirectory.resolve(key);
        Files.createDirectories(styleDirectory);
        Files.writeString(
                styleDirectory.resolve("style.json"),
                GSON.toJson(style),
                StandardCharsets.UTF_8
        );
        Files.writeString(
                styleDirectory.resolve("model.json"),
                GSON.toJson(model),
                StandardCharsets.UTF_8
        );
        Files.copy(sourceSkin, styleDirectory.resolve("skin.png"), StandardCopyOption.REPLACE_EXISTING);
        rebuildGeneratedPack();
        return id;
    }

    public static boolean delete(DollStyle style) {
        if (!style.userCreated() || stylesDirectory == null) {
            return false;
        }
        Path target = stylesDirectory.resolve(style.id().getPath()).normalize();
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
                    .sorted(Comparator.comparing(Path::toString))
                    .forEach(DollLocalStyleStore::copyStyleToGeneratedPack);
        }
    }

    private static void copyStyleToGeneratedPack(Path styleDirectory) {
        try {
            Path metadataFile = styleDirectory.resolve("style.json");
            Path modelFile = styleDirectory.resolve("model.json");
            Path skinFile = styleDirectory.resolve("skin.png");
            if (!Files.isRegularFile(metadataFile)
                    || !Files.isRegularFile(modelFile)
                    || !Files.isRegularFile(skinFile)) {
                throw new IOException("Missing style.json, model.json, or skin.png");
            }
            validateSkin(skinFile);
            JsonObject metadata = GSON.fromJson(
                    Files.readString(metadataFile, StandardCharsets.UTF_8),
                    JsonObject.class
            );
            ResourceLocation id = requireLocation(metadata, "id");
            if (!id.getNamespace().equals(Constants.MOD_ID)
                    || !id.getPath().equals(styleDirectory.getFileName().toString())) {
                throw new IOException("Style id does not match its directory");
            }
            JsonObject modelObject = metadata.getAsJsonObject("model");
            ResourceLocation declaredModel = requireLocation(modelObject, "path");
            String modelPath = declaredModel.getPath();
            if (modelPath.startsWith("models/")) {
                modelPath = modelPath.substring("models/".length());
            }

            Path assets = generatedPackDirectory.resolve("assets");
            Path dollTarget = assets.resolve(id.getNamespace())
                    .resolve("dolls/generated")
                    .resolve(id.getPath())
                    .resolve("doll.json");
            Path modelTarget = assets.resolve(declaredModel.getNamespace())
                    .resolve("models")
                    .resolve(modelPath + ".json");
            Path skinTarget = assets.resolve(id.getNamespace())
                    .resolve("textures/item/generated")
                    .resolve(id.getPath())
                    .resolve("skin.png");
            Files.createDirectories(dollTarget.getParent());
            Files.createDirectories(modelTarget.getParent());
            Files.createDirectories(skinTarget.getParent());
            Files.copy(metadataFile, dollTarget, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(modelFile, modelTarget, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(skinFile, skinTarget, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            Constants.LOG.error("Could not load local doll style {}", styleDirectory, exception);
        }
    }

    private static JsonObject readTemplateModel(
            DollStyle template,
            ResourceManager resourceManager
    ) throws IOException {
        ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath(
                template.model().getNamespace(),
                "models/" + template.model().getPath() + ".json"
        );
        Resource resource = resourceManager.getResource(modelFile)
                .orElseThrow(() -> new IOException("Missing template model " + modelFile));
        try (Reader reader = resource.openAsReader()) {
            JsonObject model = GSON.fromJson(reader, JsonObject.class);
            if (model == null) {
                throw new IOException("Template model is empty");
            }
            return model;
        }
    }

    private static void replaceTexture(JsonObject model, String slot, String texture) throws IOException {
        JsonObject textures = model.getAsJsonObject("textures");
        if (textures == null || !textures.has(slot)) {
            throw new IOException("Template model is missing texture slot " + slot);
        }
        String previous = textures.get(slot).getAsString();
        for (String key : textures.keySet()) {
            JsonElement value = textures.get(key);
            if (value.isJsonPrimitive() && previous.equals(value.getAsString())) {
                textures.addProperty(key, texture);
            }
        }
        textures.addProperty(slot, texture);
    }

    private static void validateSkin(Path path) throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
            throw new IOException("Skin must be a valid 64x64 PNG image");
        }
    }

    private static ResourceLocation requireLocation(JsonObject object, String member) throws IOException {
        if (object == null || !object.has(member)) {
            throw new IOException("Missing " + member);
        }
        ResourceLocation location = ResourceLocation.tryParse(object.get(member).getAsString());
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
