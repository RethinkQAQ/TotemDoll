package com.rethinkqaq.totemdoll.doll;

import java.util.List;

public record DollAnimationDefinition(
        String id,
        List<String> frames,
        int frameDuration,
        String trigger,
        int minInterval,
        int maxInterval
) {
    public boolean isRandomIdle() { return "random_idle".equals(trigger); }

    public boolean isLoop() { return "loop".equals(trigger); }

    public boolean isEventDriven() {
        return "on_screen_open".equals(trigger)
                || "on_totem_activate".equals(trigger)
                || "manual".equals(trigger);
    }
}
