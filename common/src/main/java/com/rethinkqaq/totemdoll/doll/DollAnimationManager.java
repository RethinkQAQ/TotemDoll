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

package com.rethinkqaq.totemdoll.doll;

import com.rethinkqaq.totemdoll.doll.bone.DollBoneActionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;

public final class DollAnimationManager {
    private static final Map<String, State> STATES = new HashMap<>();
    private static final Map<ResourceLocation, Integer> ACTIVATION_TICKS = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static synchronized void tick() {
        STATES.values().forEach(State::tick);
        ACTIVATION_TICKS.replaceAll((id, ticks) -> ticks - 1);
        ACTIVATION_TICKS.values().removeIf(ticks -> ticks <= 0);
        DollBoneActionManager.tick();
    }

    public static synchronized int currentFrame(DollStyle style, String animationId) {
        DollAnimationDefinition animation = style.animation(animationId);
        if (animation == null || animation.frames().isEmpty()) return 0;
        State state = STATES.computeIfAbsent(style.id() + "#" + animationId,
                ignored -> new State(animation));
        return state.frame;
    }

    public static synchronized DollAnimationDefinition displayAnimation(DollStyle style) {
        if (isTotemActivationActive(style)) {
            return style.animations().stream()
                    .filter(animation -> "on_totem_activate".equals(animation.trigger()))
                    .findFirst().orElse(null);
        }
        return style.animations().stream().findFirst().orElse(null);
    }

    public static synchronized void reset(DollStyle style) {
        STATES.keySet().removeIf(key -> key.startsWith(style.id() + "#"));
        ACTIVATION_TICKS.remove(style.id());
        DollBoneActionManager.reset(style);
    }

    public static synchronized void trigger(DollStyle style, String trigger) {
        DollBoneActionManager.trigger(style, trigger);
        if ("on_totem_activate".equals(trigger)) ACTIVATION_TICKS.put(style.id(), 40);
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

    public static synchronized boolean isTotemActivationActive(DollStyle style) {
        return ACTIVATION_TICKS.getOrDefault(style.id(), 0) > 0;
    }

    public static synchronized void clear() {
        STATES.clear();
        ACTIVATION_TICKS.clear();
        DollBoneActionManager.clear();
    }

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
