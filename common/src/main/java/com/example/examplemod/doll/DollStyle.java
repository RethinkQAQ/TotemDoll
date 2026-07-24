package com.example.examplemod.doll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public record DollStyle(
        ResourceLocation id,
        String displayName,
        String translationKey,
        ResourceLocation model,
        boolean usesCustomModel,
        ResourceLocation templateId,
        boolean userCreated,
        DollSkinDefinition skin,
        DollStyleOrigin origin
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
}
