package com.rethinkqaq.totemdoll.doll.bone;

public record BonePose(float x, float y, float z, float xRot, float yRot, float zRot,
                       float xScale, float yScale, float zScale) {
    public static final BonePose IDENTITY = new BonePose(0, 0, 0, 0, 0, 0, 1, 1, 1);
}
