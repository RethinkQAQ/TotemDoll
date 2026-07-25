package com.example.examplemod.doll.bone;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class DollBoneModels {
    private static final Map<ResourceLocation, DollBoneModel> MODELS = new HashMap<>();

    public static synchronized void put(ResourceLocation styleId, DollBoneModel model) { MODELS.put(styleId, model); }
    public static synchronized DollBoneModel get(ResourceLocation styleId) { return MODELS.get(styleId); }
    public static synchronized boolean contains(ResourceLocation styleId) { return MODELS.containsKey(styleId); }
    public static synchronized void clear() { MODELS.clear(); }

    private DollBoneModels() {}
}
