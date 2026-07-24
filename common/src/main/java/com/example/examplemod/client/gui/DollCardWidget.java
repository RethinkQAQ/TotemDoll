package com.example.examplemod.client.gui;

import com.example.examplemod.client.DollPreviewContext;
import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.doll.DollStyle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class DollCardWidget extends AbstractWidget {

    public static final int CARD_WIDTH = 112;
    public static final int CARD_HEIGHT = 86;

    private final DollStyle style;
    private final Font font;
    private final Runnable onSelect;

    public DollCardWidget(int x, int y, DollStyle style, Font font, Runnable onSelect) {
        super(x, y, CARD_WIDTH, CARD_HEIGHT, style.label());
        this.style = style;
        this.font = font;
        this.onSelect = onSelect;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = TotemDollConfig.selectedStyle().id().equals(style.id());
        int border = selected ? 0xFF55FF55 : isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF707070;
        int background = selected ? 0xCC183818 : isHoveredOrFocused() ? 0xCC383838 : 0xCC202020;

        graphics.fill(getX(), getY(), getRight(), getBottom(), border);
        graphics.fill(getX() + 2, getY() + 2, getRight() - 2, getBottom() - 2, background);

        graphics.pose().pushPose();
        graphics.pose().translate(getX() + CARD_WIDTH / 2.0F, getY() + 8.0F, 100.0F);
        graphics.pose().scale(2.75F, 2.75F, 2.75F);
        DollPreviewContext.renderAs(
                style,
                () -> graphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), -8, 0)
        );
        graphics.pose().popPose();

        graphics.drawCenteredString(
                font,
                style.label(),
                getX() + CARD_WIDTH / 2,
                getY() + 58,
                0xFFFFFF
        );
        if (selected) {
            graphics.drawCenteredString(
                    font,
                    Component.translatable("screen.totemdoll.current"),
                    getX() + CARD_WIDTH / 2,
                    getY() + 71,
                    0x80FF80
            );
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onSelect.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
