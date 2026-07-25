package com.rethinkqaq.totemdoll.doll;

import com.rethinkqaq.totemdoll.doll.bone.DollBoneActionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class DollAnimationManager {
    private static final Map<String, State> STATES = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static synchronized void tick() {
        STATES.values().forEach(State::tick);
        DollBoneActionManager.tick();
    }

    public static synchronized int currentFrame(DollStyle style, String animationId) {
        DollAnimationDefinition animation = style.animation(animationId);
        if (animation == null || animation.frames().isEmpty()) return 0;
        State state = STATES.computeIfAbsent(style.id() + "#" + animationId,
                ignored -> new State(animation));
        return state.frame;
    }

    public static synchronized void reset(DollStyle style) {
        STATES.keySet().removeIf(key -> key.startsWith(style.id() + "#"));
        DollBoneActionManager.reset(style);
    }

    public static synchronized void trigger(DollStyle style, String trigger) {
        DollBoneActionManager.trigger(style, trigger);
        for (DollAnimationDefinition animation : style.animations()) {
            if (animation.trigger().equals(trigger)) {
                state(style, animation).start();
            }
        }
    }

    public static synchronized void triggerAnimation(DollStyle style, String animationId) {
        DollBoneActionManager.triggerAnimation(style, animationId);
        DollAnimationDefinition animation = style.animation(animationId);
        if (animation != null) state(style, animation).start();
    }

    public static synchronized void clear() { STATES.clear(); DollBoneActionManager.clear(); }

    private static State state(DollStyle style, DollAnimationDefinition animation) {
        return STATES.computeIfAbsent(style.id() + "#" + animation.id(), ignored -> new State(animation));
    }

    private static final class State {
        private final DollAnimationDefinition animation;
        private int frame;
        private int remaining;
        private int wait;
        private boolean playing;

        private State(DollAnimationDefinition animation) {
            this.animation = animation;
            this.wait = animation.isRandomIdle() ? randomInterval(animation) : 0;
            this.playing = animation.isLoop();
            this.remaining = animation.isLoop() ? Math.max(0, animation.frameDuration() - 1) : 0;
        }

        private void tick() {
            if (animation.isRandomIdle() && !playing) {
                if (wait > 0) { wait--; return; }
                playing = true;
            }
            if (!playing) return;
            if (remaining > 0) { remaining--; return; }
            frame++;
            if (frame >= animation.frames().size()) {
                frame = 0;
                if (animation.isRandomIdle()) {
                    playing = false;
                    wait = randomInterval(animation);
                } else if (animation.isEventDriven()) {
                    playing = false;
                }
            } else {
                remaining = Math.max(0, animation.frameDuration() - 1);
            }
        }

        private void start() {
            frame = 0;
            remaining = Math.max(0, animation.frameDuration() - 1);
            wait = 0;
            playing = true;
        }

        private static int randomInterval(DollAnimationDefinition animation) {
            int min = Math.max(0, animation.minInterval());
            int max = Math.max(min, animation.maxInterval());
            return min == max ? min : min + RANDOM.nextInt(max - min + 1);
        }
    }

    private DollAnimationManager() {}
}
