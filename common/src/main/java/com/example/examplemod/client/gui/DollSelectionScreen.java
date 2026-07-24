package com.example.examplemod.client.gui;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.doll.DollStyle;
import com.example.examplemod.doll.DollStyles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class DollSelectionScreen extends Screen {

    private static final int CARD_GAP = 10;
    private static final int GRID_TOP = 54;
    private static final int FOOTER_HEIGHT = 36;

    private final Screen parent;
    private final List<DollCardWidget> cards = new ArrayList<>();
    private int scrollOffset;
    private int contentHeight;

    public DollSelectionScreen(Screen parent) {
        super(Component.translatable("screen.totemdoll.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cards.clear();
        for (DollStyle style : DollStyles.all()) {
            DollCardWidget card = new DollCardWidget(
                    0,
                    0,
                    style,
                    this.font,
                    () -> TotemDollConfig.select(style.id())
            );
            cards.add(card);
            this.addRenderableWidget(card);
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
        layoutCards();
    }

    private void layoutCards() {
        int availableWidth = Math.max(DollCardWidget.CARD_WIDTH, this.width - 32);
        int columns = Math.max(1, availableWidth / (DollCardWidget.CARD_WIDTH + CARD_GAP));
        int gridWidth = columns * DollCardWidget.CARD_WIDTH + (columns - 1) * CARD_GAP;
        int startX = (this.width - gridWidth) / 2;

        for (int index = 0; index < cards.size(); index++) {
            int row = index / columns;
            int column = index % columns;
            DollCardWidget card = cards.get(index);
            card.setX(startX + column * (DollCardWidget.CARD_WIDTH + CARD_GAP));
            card.setY(GRID_TOP + row * (DollCardWidget.CARD_HEIGHT + CARD_GAP) - scrollOffset);
            card.visible = card.getBottom() > GRID_TOP && card.getY() < this.height - FOOTER_HEIGHT;
        }

        int rows = (cards.size() + columns - 1) / columns;
        contentHeight = rows * DollCardWidget.CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        int viewportHeight = this.height - GRID_TOP - FOOTER_HEIGHT;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.totemdoll.choose"),
                this.width / 2,
                34,
                0xA0A0A0
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
