package com.example.examplemod.doll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import com.example.examplemod.doll.bone.DollBoneModels;

public record DollStyle(
        ResourceLocation id,
        String displayName,
        String translationKey,
        ResourceLocation model,
        boolean usesCustomModel,
        ResourceLocation templateId,
        boolean userCreated,
        DollSkinDefinition skin,
        DollStyleOrigin origin,
        Map<String, ResourceLocation> textures,
        List<DollAnimationDefinition> animations,
        String modelType,
        ResourceLocation definitionSource
) {

    public Component label() {
        return translationKey == null || translationKey.isBlank()
                ? Component.literal(displayName)
                : Component.translatable(translationKey);
    }

    public boolean supportsSkin() {
        return skin != null;
    }

    public boolean isTemplate() {
        return !userCreated && supportsSkin();
    }

    public boolean isLocal() {
        return userCreated || origin == DollStyleOrigin.LOCAL;
    }

    public boolean hasAnimations() {
        return (animations != null && !animations.isEmpty()) || DollBoneModels.contains(id);
    }

    public boolean isBoneModel() {
        return "minecraft_bone".equals(modelType);
    }

    public boolean hasDynamicModel() {
        return isBoneModel() && DollBoneModels.contains(id);
    }

    public boolean hasDynamicTextures() {
        return animations != null && !animations.isEmpty();
    }

    public DollAnimationDefinition animation(String id) {
        return animations.stream().filter(animation -> animation.id().equals(id)).findFirst().orElse(null);
    }
}
