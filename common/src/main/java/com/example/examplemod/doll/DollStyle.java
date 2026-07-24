package com.example.examplemod.doll;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public record DollStyle(
        ResourceLocation id,
        String displayName,
        String translationKey,
        ResourceLocation model,
        boolean usesCustomModel
) {

    public Component label() {
        return translationKey == null || translationKey.isBlank()
                ? Component.literal(displayName)
                : Component.translatable(translationKey);
    }
}
