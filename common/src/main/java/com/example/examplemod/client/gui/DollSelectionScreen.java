package com.example.examplemod.client.gui;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.client.TotemDollClient;
import com.example.examplemod.doll.DollLocalStyleStore;
import com.example.examplemod.doll.DollStyle;
import com.example.examplemod.doll.DollStyles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DollSelectionScreen extends Screen {

    private static final int CARD_GAP = 10;
    private static final int GRID_TOP = 54;
    private static final int FOOTER_HEIGHT = 60;

    private final Screen parent;
    private final List<DollCardWidget> cards = new ArrayList<>();
    private int scrollOffset;
    private int contentHeight;
    private Button createButton;
    private Button deleteButton;
    private Component status;

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

        createButton = this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.create"),
                button -> importSkin()
        ).bounds(this.width / 2 - 154, this.height - 52, 100, 20).build());
        deleteButton = this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.delete"),
                button -> deleteSelected()
        ).bounds(this.width / 2 - 50, this.height - 52, 100, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(this.width / 2 + 54, this.height - 52, 100, 20).build());
        updateActions();
        layoutCards();
    }

    private void updateActions() {
        DollStyle selected = TotemDollConfig.selectedStyle();
        createButton.active = selected.isTemplate();
        deleteButton.active = selected.userCreated();
    }

    private void importSkin() {
        DollStyle template = TotemDollConfig.selectedStyle();
        if (!template.isTemplate() || this.minecraft == null) {
            return;
        }
        String chosen = TinyFileDialogs.tinyfd_openFileDialog(
                "Select a 64x64 Minecraft skin",
                "",
                null,
                "PNG image",
                false
        );
        if (chosen == null) {
            return;
        }
        try {
            Path skin = Path.of(chosen);
            var createdId = DollLocalStyleStore.importSkin(
                    template,
                    skin,
                    skin.getFileName().toString().replaceFirst("(?i)\\.png$", ""),
                    this.minecraft.getResourceManager()
            );
            status = Component.translatable("screen.totemdoll.loading");
            TotemDollClient.reloadGeneratedStyles().thenRun(() -> this.minecraft.execute(() -> {
                TotemDollConfig.select(createdId);
                this.minecraft.setScreen(new DollSelectionScreen(parent));
            }));
        } catch (IOException exception) {
            status = Component.translatable("screen.totemdoll.import_failed", exception.getMessage());
        }
    }

    private void deleteSelected() {
        DollStyle selected = TotemDollConfig.selectedStyle();
        if (!selected.userCreated() || this.minecraft == null) {
            return;
        }
        if (DollLocalStyleStore.delete(selected)) {
            TotemDollConfig.select(DollStyles.ALEX_ID);
            status = Component.translatable("screen.totemdoll.loading");
            TotemDollClient.reloadGeneratedStyles().thenRun(() -> this.minecraft.execute(() ->
                    this.minecraft.setScreen(new DollSelectionScreen(parent))
            ));
        } else {
            status = Component.translatable("screen.totemdoll.delete_failed");
        }
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
        if (status != null) {
            graphics.drawCenteredString(
                    this.font,
                    status,
                    this.width / 2,
                    this.height - 24,
                    0xFF8080
            );
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        updateActions();
    }
}
