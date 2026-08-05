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

package com.rethinkqaq.totemdoll.client.gui;

import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
//? >= 1.21.9 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}

public final class DollCardWidget extends AbstractWidget {

    public static final int CARD_WIDTH = 168;
    public static final int CARD_HEIGHT = 116;
    private static final int ACTION_HEIGHT = 20;

    private final DollStyle style;
    private final Font font;
    private final Runnable onUse;
    private final Runnable onSecondary;
    private final Component secondaryLabel;

    public DollCardWidget(
            int x,
            int y,
            DollStyle style,
            Font font,
            Runnable onUse,
            Runnable onSecondary,
            Component secondaryLabel
    ) {
        super(x, y, CARD_WIDTH, CARD_HEIGHT, style.label());
        this.style = style;
        this.font = font;
        this.onUse = onUse;
        this.onSecondary = onSecondary;
        this.secondaryLabel = secondaryLabel;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = TotemDollConfig.selectedStyle().id().equals(style.id());
        int border = selected ? 0xFF5FD776 : isHoveredOrFocused() ? 0xFFD0D0D0 : 0xFF585858;
        int background = selected ? 0xFF203827 : 0xFF202020;
        graphics.fill(getX(), getY(), getRight(), getBottom(), border);
        graphics.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, background);

        DollGuiPreview.render(graphics, style, getX() + 5, getY() + 5, 52, 52, 37.6F);

        int textX = getX() + 61;
        int textWidth = getRight() - textX - 7;
        graphics.drawString(font, trim(style.label(), textWidth), textX, getY() + 11, 0xFFFFFFFF);
        graphics.drawString(font, trim(originLabel(), textWidth), textX, getY() + 27, 0xFFA0A0A0);
        int detailY = 42;
        if (style.supportsSkin()) {
            drawDetail(graphics, Component.translatable("screen.totemdoll.skin_supported"), textX, detailY, textWidth, 0xFF80C88A);
            detailY += 15;
        }
        if (style.hasDynamicModel() || style.hasDynamicTextures()) {
            Component capability = style.hasDynamicModel() && style.hasDynamicTextures()
                    ? Component.translatable("screen.totemdoll.dynamic_both")
                    : style.hasDynamicModel()
                    ? Component.translatable("screen.totemdoll.dynamic_model")
                    : Component.translatable("screen.totemdoll.dynamic_texture");
            drawDetail(graphics, capability, textX, detailY, textWidth, 0xFF80B8E8);
            detailY += 15;
        }
        if (style.isLocal() && style.templateId() != null) {
            DollStyle template = DollStyles.get(style.templateId());
            drawDetail(graphics,
                    Component.translatable("screen.totemdoll.from_template", template.label()),
                    textX, 72, textWidth, 0xFFA0A0A0);
        }
        if (selected && !(style.isLocal() && style.templateId() != null)) {
            drawDetail(graphics, Component.translatable("screen.totemdoll.current"),
                    textX, 72, textWidth, 0xFF70E088);
        }

        renderActions(graphics, mouseX, mouseY);
    }

    private void drawDetail(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        graphics.drawString(font, trim(text, width), x, getY() + y, color);
    }

    private Component trim(Component text, int width) {
        if (font.width(text) <= width) return text;
        String value = text.getString();
        while (!value.isEmpty() && font.width(value + "...") > width)
            value = value.substring(0, value.length() - 1);
        return Component.literal(value + "...");
    }

    private void renderActions(GuiGraphics graphics, int mouseX, int mouseY) {
        int top = getBottom() - ACTION_HEIGHT - 4;
        int left = getX() + 5;
        int right = getRight() - 5;
        boolean hasSecondary = onSecondary != null;
        int split = hasSecondary ? getX() + getWidth() / 2 : right;

        boolean useHovered = mouseX >= left && mouseX < split
                && mouseY >= top && mouseY < getBottom() - 4;
        graphics.fill(left, top, split - (hasSecondary ? 2 : 0), getBottom() - 4,
                useHovered ? 0xFF3E7350 : 0xFF315A40);
        graphics.drawCenteredString(
                font,
                Component.translatable("screen.totemdoll.use"),
                (left + split - (hasSecondary ? 2 : 0)) / 2,
                top + 6,
                0xFFFFFFFF
        );

        if (hasSecondary) {
            boolean secondaryHovered = mouseX >= split + 2 && mouseX < right
                    && mouseY >= top && mouseY < getBottom() - 4;
            graphics.fill(split + 2, top, right, getBottom() - 4,
                    secondaryHovered ? 0xFF556B83 : 0xFF3D5065);
            graphics.drawCenteredString(
                    font,
                    secondaryLabel,
                    (split + 2 + right) / 2,
                    top + 6,
                    0xFFFFFFFF
            );
        }
    }

    private Component originLabel() {
        return switch (style.origin()) {
            case BUILTIN -> Component.translatable("screen.totemdoll.origin_builtin");
            case RESOURCE_PACK -> Component.translatable("screen.totemdoll.origin_resource_pack");
            case LOCAL -> Component.translatable("screen.totemdoll.origin_local");
        };
    }

    @Override
    //? >= 1.21.9 {
    /*public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        handleClick(event.x(), event.y());
    }
    *///?} else {
    public void onClick(double mouseX, double mouseY) {
        handleClick(mouseX, mouseY);
    }
    //?}

    private void handleClick(double mouseX, double mouseY) {
        double relativeX = mouseX - getX();
        double relativeY = mouseY - getY();
        int actionTop = getHeight() - ACTION_HEIGHT - 4;
        if (relativeY >= actionTop && onSecondary != null && relativeX >= getWidth() / 2.0D) {
            onSecondary.run();
            return;
        }
        onUse.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
