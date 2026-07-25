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

import com.rethinkqaq.totemdoll.doll.DollStyle;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class DollBoneActionManager {
    private static final int TOTEM_ACTIVATION_DURATION = 40;
    private static final Map<ResourceLocation, State> STATES = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static synchronized void tick() {
        STATES.values().forEach(State::tick);
    }

    public static synchronized void trigger(DollStyle style, String trigger) {
        DollBoneModel model = DollBoneModels.get(style.id());
        if (model == null) return;
        DollActionBinding selected = model.bindings().stream()
                .filter(binding -> binding.trigger().equals(trigger))
                .max(java.util.Comparator.comparingInt(DollActionBinding::priority)).orElse(null);
        if (selected != null) state(style.id(), model).start(selected);
    }

    public static synchronized void triggerAnimation(DollStyle style, String id) {
        DollBoneModel model = DollBoneModels.get(style.id());
        if (model == null) return;
        model.bindings().stream().filter(binding -> binding.id().equals(id)).findFirst()
                .ifPresent(binding -> state(style.id(), model).start(binding));
    }

    public static synchronized BonePose pose(DollStyle style, String bone, float partialTick) {
        DollBoneModel model = DollBoneModels.get(style.id());
        if (model == null) return BonePose.IDENTITY;
        State state = state(style.id(), model);
        if (state.active == null) return BonePose.IDENTITY;
        DollBoneAnimation animation = model.animations().get(state.active.animation());
        if (animation == null) return BonePose.IDENTITY;
        DollBoneTimeline timeline = animation.bones().get(bone);
        return timeline == null ? BonePose.IDENTITY : timeline.sample(state.time + partialTick);
    }

    public static synchronized void reset(DollStyle style) { STATES.remove(style.id()); }
    public static synchronized void clear() { STATES.clear(); }

    private static State state(ResourceLocation id, DollBoneModel model) {
        return STATES.computeIfAbsent(id, ignored -> new State(model));
    }

    private static final class State {
        private final DollBoneModel model;
        private DollActionBinding active;
        private int time;
        private int randomWait;
        private int eventTicksRemaining;

        private State(DollBoneModel model) {
            this.model = model;
            scheduleRandom();
            model.bindings().stream().filter(binding -> "loop".equals(binding.trigger()))
                    .max(java.util.Comparator.comparingInt(DollActionBinding::priority)).ifPresent(this::start);
        }

        private void tick() {
            if (active == null) {
                DollActionBinding random = model.bindings().stream()
                        .filter(binding -> "random_idle".equals(binding.trigger())).findFirst().orElse(null);
                if (random != null && --randomWait <= 0) start(random);
                return;
            }
            DollBoneAnimation animation = model.animations().get(active.animation());
            if (animation == null) { active = null; return; }
            time++;
            if (eventTicksRemaining > 0) eventTicksRemaining--;
            if (time < animation.length()) return;
            if ("on_totem_activate".equals(active.trigger()) && eventTicksRemaining > 0) {
                time = 0;
                return;
            }
            // Event actions are one-shot even when Blockbench accidentally
            // exports the source animation as looped. Only idle-style
            // triggers are allowed to repeat indefinitely.
            if ("loop".equals(active.trigger()) ||
                    (animation.loop() && "random_idle".equals(active.trigger()))) {
                time = 0;
                return;
            }
            active = null;
            time = 0;
            eventTicksRemaining = 0;
            scheduleRandom();
            model.bindings().stream().filter(binding -> "loop".equals(binding.trigger()))
                    .max(java.util.Comparator.comparingInt(DollActionBinding::priority)).ifPresent(this::start);
        }

        private void start(DollActionBinding binding) {
            if (active != null && active.priority() > binding.priority()) return;
            active = binding;
            time = 0;
            eventTicksRemaining = "on_totem_activate".equals(binding.trigger())
                    ? TOTEM_ACTIVATION_DURATION : 0;
        }

        private void scheduleRandom() {
            DollActionBinding random = model.bindings().stream()
                    .filter(binding -> "random_idle".equals(binding.trigger())).findFirst().orElse(null);
            if (random == null) { randomWait = Integer.MAX_VALUE; return; }
            int min = Math.max(0, random.minInterval());
            int max = Math.max(min, random.maxInterval());
            randomWait = min == max ? min : min + RANDOM.nextInt(max - min + 1);
        }
    }

    private DollBoneActionManager() {}
}
