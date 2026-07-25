package com.rethinkqaq.totemdoll.doll.bone;

import java.util.Map;

public record DollBoneAnimation(String id, boolean loop, int length,
                                Map<String, DollBoneTimeline> bones) {}
