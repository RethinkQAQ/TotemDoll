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
import com.rethinkqaq.totemdoll.utils.DollGuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

//? >= 1.21.10 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}

public final class DollCardWidget extends DollWidget {

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
    protected void renderContent(DollGuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        boolean selected = TotemDollConfig.selectedStyle().id().equals(style.id());
        int border = selected ? 0xFF5FD776 : isHoveredOrFocused() ? 0xFFD0D0D0 : 0xFF585858;
        int background = selected ? 0xFF203827 : 0xFF202020;
        gui.fill(getX(), getY(), getRight(), getBottom(), border);
        gui.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, background);

        DollGuiPreview.render(gui, style, getX() + 5, getY() + 5, 52, 52, 37.6F);

        int textX = getX() + 61;
        int textWidth = getRight() - textX - 7;
        gui.text(font, trim(style.label(), textWidth), textX, getY() + 11, 0xFFFFFFFF);
        gui.text(font, trim(originLabel(), textWidth), textX, getY() + 27, 0xFFA0A0A0);
        int detailY = 42;
        if (!style.isAvailable()) {
            drawDetail(gui, Component.translatable("screen.totemdoll.unavailable"), textX, detailY,
                    textWidth, 0xFFFF8080);
            detailY += 15;
        }
        if (style.supportsSkin()) {
            drawDetail(gui, Component.translatable("screen.totemdoll.skin_supported"), textX, detailY, textWidth, 0xFF80C88A);
            detailY += 15;
        }
        if (style.hasDynamicModel() || style.hasDynamicTextures()) {
            Component capability = style.hasDynamicModel() && style.hasDynamicTextures()
                    ? Component.translatable("screen.totemdoll.dynamic_both")
                    : style.hasDynamicModel()
                    ? Component.translatable("screen.totemdoll.dynamic_model")
                    : Component.translatable("screen.totemdoll.dynamic_texture");
            drawDetail(gui, capability, textX, detailY, textWidth, 0xFF80B8E8);
            detailY += 15;
        }
        if (style.isLocal() && style.templateId() != null) {
            DollStyle template = DollStyles.get(style.templateId());
            drawDetail(gui,
                    Component.translatable("screen.totemdoll.from_template", template.label()),
                    textX, 72, textWidth, 0xFFA0A0A0);
        }
        if (selected && !(style.isLocal() && style.templateId() != null)) {
            drawDetail(gui, Component.translatable("screen.totemdoll.current"),
                    textX, 72, textWidth, 0xFF70E088);
        }

        renderActions(gui, mouseX, mouseY);
    }

    private void drawDetail(DollGuiGraphics graphics, Component text, int x, int y, int width, int color) {
        graphics.text(font, trim(text, width), x, getY() + y, color);
    }

    private Component trim(Component text, int width) {
        if (font.width(text) <= width) return text;
        String value = text.getString();
        while (!value.isEmpty() && font.width(value + "...") > width)
            value = value.substring(0, value.length() - 1);
        return Component.literal(value + "...");
    }

    private void renderActions(DollGuiGraphics graphics, int mouseX, int mouseY) {
        int top = getBottom() - ACTION_HEIGHT - 4;
        int left = getX() + 5;
        int right = getRight() - 5;
        boolean hasSecondary = onSecondary != null;
        int split = hasSecondary ? getX() + getWidth() / 2 : right;

        boolean useHovered = mouseX >= left && mouseX < split
                && mouseY >= top && mouseY < getBottom() - 4;
        boolean available = style.isAvailable();
        graphics.fill(left, top, split - (hasSecondary ? 2 : 0), getBottom() - 4,
                !available ? 0xFF424242 : useHovered ? 0xFF3E7350 : 0xFF315A40);
        graphics.centeredText(
                font,
                Component.translatable(available ? "screen.totemdoll.use" : "screen.totemdoll.unavailable"),
                (left + split - (hasSecondary ? 2 : 0)) / 2,
                top + 6,
                0xFFFFFFFF
        );

        if (hasSecondary) {
            boolean secondaryHovered = mouseX >= split + 2 && mouseX < right
                    && mouseY >= top && mouseY < getBottom() - 4;
            graphics.fill(split + 2, top, right, getBottom() - 4,
                    secondaryHovered ? 0xFF556B83 : 0xFF3D5065);
            graphics.centeredText(
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
            case IMPORTED -> style.packMetadata() == null
                    ? Component.translatable("screen.totemdoll.origin_imported")
                    : Component.literal(style.packMetadata().displayName());
        };
    }

    public void renderTooltip(DollGuiGraphics graphics, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!isHoveredOrFocused()) return;
        List<String> lines = new ArrayList<>();
        lines.add(style.label().getString());
        lines.add("ID: " + style.id());
        if (!style.isAvailable() && style.invalidReason() != null) {
            lines.add(Component.translatable("screen.totemdoll.unavailable").getString());
            lines.add(style.invalidReason());
        }
        if (style.packMetadata() != null) {
            var pack = style.packMetadata();
            lines.add("Pack: " + pack.displayName());
            if (pack.author() != null && !pack.author().isBlank()) lines.add("Author: " + pack.author());
            if (pack.licenseName() != null) {
                lines.add("License: " + pack.licenseName());
                addSummary(lines, pack.licenseSummary());
            }
            if (pack.readmeName() != null) {
                lines.add("README: " + pack.readmeName());
                addSummary(lines, pack.readmeSummary());
            }
        }

        int width = lines.stream().mapToInt(font::width).max().orElse(80) + 8;
        width = Math.min(width, 240);
        int lineHeight = 10;
        int height = lines.size() * lineHeight + 8;
        int left = mouseX + 12;
        int top = mouseY + 12;
        if (left + width > screenWidth) left = Math.max(4, mouseX - width - 8);
        if (top + height > screenHeight) top = Math.max(4, mouseY - height - 8);

        graphics.pushPose();
        //? < 1.21.6 {
        graphics.translate(0.0F, 0.0F, 400.0F);
        //?}
        graphics.fill(left - 1, top - 1, left + width + 1, top + height + 1, 0xFFB0B0B0);
        graphics.fill(left, top, left + width, top + height, 0xF0101010);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (font.width(line) > width - 8) {
                line = trim(Component.literal(line), width - 8).getString();
            }
            graphics.text(font, Component.literal(line), left + 4, top + 4 + index * lineHeight,
                    index == 0 ? 0xFFFFFFFF : 0xFFE0E0E0);
        }
        graphics.popPose();
    }

    private void addSummary(List<String> lines, String summary) {
        if (summary == null || summary.isBlank()) return;
        for (String line : summary.split("\\R")) {
            if (!line.isBlank()) lines.add(line);
        }
    }

    @Override
    //? >= 1.21.10 {
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
        if (style.isAvailable()) onUse.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
