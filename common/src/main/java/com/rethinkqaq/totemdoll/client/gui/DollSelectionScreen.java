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

import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DollSelectionScreen extends Screen implements DollScreenParent {

    private static final int SIDEBAR_WIDTH = 104;
    private static final int HEADER_HEIGHT = 92;
    private static final int CARD_GAP = 10;
    private static final int FOOTER_HEIGHT = 28;

    private final Screen parent;
    private final Tab tab;
    private final List<DollCardWidget> cards = new ArrayList<>();
    private int scrollOffset;
    private int contentHeight;

    public DollSelectionScreen(Screen parent) {
        this(parent, Tab.TEMPLATES);
    }

    public DollSelectionScreen(Screen parent, Tab tab) {
        super(Component.translatable("screen.totemdoll.title"));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        // Every preview card has its own style ID and therefore its own
        // animation state. Trigger the screen-open action for all styles,
        // not only for the currently selected gameplay style.
        DollStyles.all().forEach(style ->
                DollAnimationManager.trigger(style, "on_screen_open"));
        cards.clear();
        scrollOffset = 0;
        contentHeight = 0;
        List<DollStyle> styles = DollStyles.all().stream()
                .filter(style -> tab == Tab.MY_STYLES ? style.isLocal() : !style.isLocal())
                .sorted(styleComparator())
                .toList();

        for (DollStyle style : styles) {
            DollCardWidget card = new DollCardWidget(
                    0,
                    0,
                    style,
                    this.font,
                    () -> TotemDollConfig.select(style.id()),
                    secondaryAction(style),
                    tab == Tab.MY_STYLES
                            ? Component.translatable("screen.totemdoll.manage")
                            : Component.translatable("screen.totemdoll.create_short")
            );
            cards.add(card);
            this.addRenderableWidget(card);
        }
        this.addRenderableWidget(Button.builder(
                tabLabel(Tab.TEMPLATES),
                button -> switchTab(Tab.TEMPLATES)
        ).bounds(14, HEADER_HEIGHT + 28, SIDEBAR_WIDTH - 22, 20).build());
        this.addRenderableWidget(Button.builder(
                tabLabel(Tab.MY_STYLES),
                button -> switchTab(Tab.MY_STYLES)
        ).bounds(14, HEADER_HEIGHT + 54, SIDEBAR_WIDTH - 22, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(this.width - 108, this.height - 24, 96, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.import_pack"),
                button -> DollPackScreen.chooseZipAndImport(this)
        ).bounds(this.width - 220, this.height - 24, 104, 20).build());
        layoutCards();
    }

    private Runnable secondaryAction(DollStyle style) {
        if (tab == Tab.MY_STYLES) {
            return () -> this.minecraft.setScreen(new DollStyleManageScreen(this, style));
        }
        if (style.supportsSkin()) {
            return () -> this.minecraft.setScreen(new DollCreateScreen(this, style));
        }
        return null;
    }

    private Component tabLabel(Tab target) {
        Component label = Component.translatable(target == Tab.TEMPLATES
                ? "screen.totemdoll.templates"
                : "screen.totemdoll.my_styles");
        return target == tab ? Component.literal("> ").append(label) : label;
    }

    private void switchTab(Tab target) {
        if (target != tab) {
            this.minecraft.setScreen(new DollSelectionScreen(parent, target));
        }
    }

    private Comparator<DollStyle> styleComparator() {
        return Comparator.comparingInt(this::styleOrder)
                .thenComparing(style -> style.label().getString(), String.CASE_INSENSITIVE_ORDER);
    }

    private int styleOrder(DollStyle style) {
        if (tab == Tab.MY_STYLES) {
            return TotemDollConfig.selectedStyle().id().equals(style.id()) ? 0 : 10;
        }
        if (DollStyles.VANILLA_ID.equals(style.id())) {
            return 0;
        }
        if (DollStyles.ALEX_ID.equals(style.id())) {
            return 10;
        }
        if ("totemdoll:steve".equals(style.id().toString())) {
            return 20;
        }
        return switch (style.origin()) {
            case BUILTIN -> 100;
            case RESOURCE_PACK -> 200;
            case LOCAL -> 300;
        };
    }

    private void layoutCards() {
        int left = SIDEBAR_WIDTH + 18;
        int top = HEADER_HEIGHT + 22;
        int availableWidth = Math.max(DollCardWidget.CARD_WIDTH, this.width - left - 18);
        int columns = Math.max(1, (availableWidth + CARD_GAP) / (DollCardWidget.CARD_WIDTH + CARD_GAP));
        int startX = left;
        int viewportBottom = this.height - FOOTER_HEIGHT;

        for (int index = 0; index < cards.size(); index++) {
            int row = index / columns;
            int column = index % columns;
            DollCardWidget card = cards.get(index);
            card.setX(startX + column * (DollCardWidget.CARD_WIDTH + CARD_GAP));
            card.setY(top + row * (DollCardWidget.CARD_HEIGHT + CARD_GAP) - scrollOffset);
            card.visible = card.getBottom() > top && card.getY() < viewportBottom;
        }

        int rows = (cards.size() + columns - 1) / columns;
        contentHeight = rows * DollCardWidget.CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int top = HEADER_HEIGHT + 22;
        int viewportHeight = this.height - top - FOOTER_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - viewportHeight);
        int nextOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * 24)));
        if (nextOffset != scrollOffset) {
            scrollOffset = nextOffset;
            layoutCards();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public Screen rootParent() {
        return parent;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
        renderHeader(graphics);
        renderSidebar(graphics);
        if (cards.isEmpty()) {
            Component emptyTitle = Component.translatable("screen.totemdoll.empty_title");
            Component emptyHint = Component.translatable("screen.totemdoll.empty_hint");
            int centerX = SIDEBAR_WIDTH + (this.width - SIDEBAR_WIDTH) / 2;
            graphics.drawCenteredString(this.font, emptyTitle, centerX, HEADER_HEIGHT + 72, 0xFFFFFF);
            graphics.drawCenteredString(this.font, emptyHint, centerX, HEADER_HEIGHT + 88, 0xA0A0A0);
        }
        DollScreenRender.renderChildren(this, graphics, mouseX, mouseY, partialTick);
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.fill(8, 8, this.width - 8, HEADER_HEIGHT, 0xFF181818);
        graphics.fill(8, HEADER_HEIGHT - 2, this.width - 8, HEADER_HEIGHT, 0xFF4A4A4A);
        DollStyle selected = TotemDollConfig.selectedStyle();
        graphics.pose().pushPose();
        graphics.pose().translate(48.0F, 21.0F, 100.0F);
        graphics.pose().scale(3.4F, 3.4F, 3.4F);
        DollPreviewContext.renderAs(
                selected,
                () -> graphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING), -8, 0)
        );
        graphics.pose().popPose();
        graphics.drawString(this.font, Component.translatable("screen.totemdoll.current_preview"), 82, 18, 0xA0A0A0);
        graphics.drawString(this.font, selected.label(), 82, 36, 0xFFFFFF);
        graphics.drawString(
                this.font,
                Component.translatable("screen.totemdoll.current_type", selected.isLocal()
                        ? Component.translatable("screen.totemdoll.origin_local")
                        : Component.translatable("screen.totemdoll.template")),
                82,
                53,
                0xA0A0A0
        );
        if (selected.isLocal() && selected.templateId() != null) {
            graphics.drawString(
                    this.font,
                    Component.translatable(
                            "screen.totemdoll.from_template",
                            DollStyles.get(selected.templateId()).label()
                    ),
                    82,
                    68,
                    0x8FB4D8
            );
        }
    }

    private void renderSidebar(GuiGraphics graphics) {
        graphics.fill(8, HEADER_HEIGHT + 8, SIDEBAR_WIDTH - 4, this.height - FOOTER_HEIGHT, 0xFF181818);
        graphics.fill(SIDEBAR_WIDTH - 6, HEADER_HEIGHT + 8, SIDEBAR_WIDTH - 4, this.height - FOOTER_HEIGHT, 0xFF3D3D3D);
        graphics.drawString(this.font, Component.translatable("screen.totemdoll.library"), 18, HEADER_HEIGHT + 18, 0xA0A0A0);
        int indicatorY = tab == Tab.TEMPLATES ? HEADER_HEIGHT + 28 : HEADER_HEIGHT + 54;
        graphics.fill(10, indicatorY, 13, indicatorY + 20, 0xFF55D878);
    }

    public enum Tab {
        TEMPLATES,
        MY_STYLES
    }
}
