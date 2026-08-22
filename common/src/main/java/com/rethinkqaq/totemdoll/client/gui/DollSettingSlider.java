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
 */

package com.rethinkqaq.totemdoll.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;

final class DollSettingSlider extends AbstractSliderButton {
    private final Function<Double, Component> messageFactory;
    private final Consumer<Double> valueConsumer;

    DollSettingSlider(int x, int y, int width, int height, double value,
                      Function<Double, Component> messageFactory, Consumer<Double> valueConsumer) {
        super(x, y, width, height, Component.empty(), value);
        this.messageFactory = messageFactory;
        this.valueConsumer = valueConsumer;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(messageFactory.apply(value));
    }

    @Override
    protected void applyValue() {
        valueConsumer.accept(value);
    }
}
