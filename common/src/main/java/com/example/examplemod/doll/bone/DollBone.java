package com.example.examplemod.doll.bone;

import java.util.List;

public record DollBone(String name, float pivotX, float pivotY, float pivotZ,
                       float rotationX, float rotationY, float rotationZ,
                       List<DollCube> cubes, List<DollBone> children) {}
