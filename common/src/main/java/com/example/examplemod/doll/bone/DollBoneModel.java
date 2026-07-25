package com.example.examplemod.doll.bone;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record DollBoneModel(int textureWidth, int textureHeight, ResourceLocation texture,
                            List<DollBone> roots, Map<String, DollBoneAnimation> animations,
                            List<DollActionBinding> bindings,
                            Map<String, DollDisplayTransform> display) {}
