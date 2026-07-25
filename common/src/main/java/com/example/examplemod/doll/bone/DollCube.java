package com.example.examplemod.doll.bone;

import java.util.Map;

public record DollCube(float x, float y, float z, float width, float height, float depth,
                       int u, int v, boolean mirror, Map<String, DollFace> faces) {}
