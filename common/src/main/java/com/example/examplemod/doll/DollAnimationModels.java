package com.example.examplemod.doll;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class DollAnimationModels {
    public static List<ResourceLocation> modelIds(DollStyle style) {
        List<ResourceLocation> ids = new ArrayList<>();
        for (DollAnimationDefinition animation : style.animations()) {
            for (String frame : animation.frames()) {
                ids.add(frameModelId(style, frame));
            }
        }
        return ids.stream().distinct().toList();
    }

    public static ResourceLocation frameModelId(DollStyle style, String frame) {
        return ResourceLocation.fromNamespaceAndPath(style.model().getNamespace(),
                style.model().getPath() + "__" + frame);
    }

    private DollAnimationModels() {}
}
