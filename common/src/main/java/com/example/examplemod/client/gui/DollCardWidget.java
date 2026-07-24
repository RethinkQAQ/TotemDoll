package com.example.examplemod.client.gui;

import com.example.examplemod.client.DollPreviewContext;
import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.doll.DollStyle;
import com.example.examplemod.doll.DollStyles;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

        graphics.pose().pushPose();
        graphics.pose().translate(getX() + 34.0F, getY() + 8.0F, 100.0F);
        graphics.pose().scale(2.35F, 2.35F, 2.35F);
        DollPreviewContext.renderAs(
                style,
                () -> graphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), -8, 0)
        );
        graphics.pose().popPose();

        graphics.drawString(font, style.label(), getX() + 61, getY() + 11, 0xFFFFFF);
        graphics.drawString(font, originLabel(), getX() + 61, getY() + 27, 0xA0A0A0);
        if (style.supportsSkin()) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.totemdoll.skin_supported"),
                    getX() + 61,
                    getY() + 42,
                    0x80C88A
            );
        }
        if (style.isLocal() && style.templateId() != null) {
            DollStyle template = DollStyles.get(style.templateId());
            graphics.drawString(
                    font,
                    Component.translatable("screen.totemdoll.from_template", template.label()),
                    getX() + 61,
                    getY() + 57,
                    0xA0A0A0
            );
        }
        if (selected) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.totemdoll.current"),
                    getX() + 61,
                    getY() + 72,
                    0x70E088
            );
        }

        renderActions(graphics, mouseX, mouseY);
    }

    private void renderActions(GuiGraphics graphics, int mouseX, int mouseY) {
        int top = getBottom() - ACTION_HEIGHT - 4;
        int left = getX() + 5;
        int right = getRight() - 5;
        boolean hasSecondary = onSecondary != null;
        int split = hasSecondary ? getX() + CARD_WIDTH / 2 : right;

        boolean useHovered = mouseX >= left && mouseX < split
                && mouseY >= top && mouseY < getBottom() - 4;
        graphics.fill(left, top, split - (hasSecondary ? 2 : 0), getBottom() - 4,
                useHovered ? 0xFF3E7350 : 0xFF315A40);
        graphics.drawCenteredString(
                font,
                Component.translatable("screen.totemdoll.use"),
                (left + split - (hasSecondary ? 2 : 0)) / 2,
                top + 6,
                0xFFFFFF
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
                    0xFFFFFF
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
    public void onClick(double mouseX, double mouseY) {
        double relativeX = mouseX - getX();
        double relativeY = mouseY - getY();
        int actionTop = getHeight() - ACTION_HEIGHT - 4;
        if (relativeY >= actionTop && onSecondary != null && relativeX >= CARD_WIDTH / 2.0D) {
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
