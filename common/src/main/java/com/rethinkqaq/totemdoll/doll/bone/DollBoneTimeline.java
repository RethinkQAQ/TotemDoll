/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 *
 * Totem Doll is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Totem Doll is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Totem Doll. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rethinkqaq.totemdoll.doll.bone;

import java.util.List;

public record DollBoneTimeline(List<DollKeyframe> rotation, List<DollKeyframe> position,
                               List<DollKeyframe> scale) {
    public BonePose sample(float time) {
        float[] rotationValue = sample(rotation, time, 0, 0, 0);
        float[] positionValue = sample(position, time, 0, 0, 0);
        float[] scaleValue = sample(scale, time, 1, 1, 1);
        return new BonePose(positionValue[0], positionValue[1], positionValue[2],
                rotationValue[0], rotationValue[1], rotationValue[2],
                scaleValue[0], scaleValue[1], scaleValue[2]);
    }

    private static float[] sample(List<DollKeyframe> frames, float time, float dx, float dy, float dz) {
        if (frames == null || frames.isEmpty()) return new float[]{dx, dy, dz};
        if (time <= frames.get(0).time()) return value(frames.get(0));
        for (int index = 1; index < frames.size(); index++) {
            DollKeyframe next = frames.get(index);
            if (time <= next.time()) {
                DollKeyframe previous = frames.get(index - 1);
                float span = Math.max(0.0001F, next.time() - previous.time());
                float progress = (time - previous.time()) / span;
                String interpolation = next.interpolation();
                if ("step".equals(interpolation)) progress = 0;
                else if ("smooth".equals(interpolation)) progress = progress * progress * (3 - 2 * progress);
                return new float[]{lerp(previous.x(), next.x(), progress),
                        lerp(previous.y(), next.y(), progress), lerp(previous.z(), next.z(), progress)};
            }
        }
        return value(frames.get(frames.size() - 1));
    }

    private static float[] value(DollKeyframe frame) { return new float[]{frame.x(), frame.y(), frame.z()}; }
    private static float lerp(float from, float to, float amount) { return from + (to - from) * amount; }
}
