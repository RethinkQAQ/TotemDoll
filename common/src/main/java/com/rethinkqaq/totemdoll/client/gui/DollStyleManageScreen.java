package com.rethinkqaq.totemdoll.client.gui;

import com.rethinkqaq.totemdoll.client.DollPreviewContext;
import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class DollStyleManageScreen extends Screen implements DollScreenParent {

    private final Screen parent;
    private final DollStyle style;
    private EditBox nameBox;
    private Component status;

    public DollStyleManageScreen(Screen parent, DollStyle style) {
        super(Component.translatable("screen.totemdoll.manage_title"));
        this.parent = parent;
        this.style = style;
    }

    @Override
    protected void init() {
        nameBox = new EditBox(this.font, this.width / 2 - 110, 90, 220, 20, Component.literal(""));
        nameBox.setMaxLength(48);
        nameBox.setValue(style.displayName());
        this.addRenderableWidget(nameBox);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.rename"),
                button -> rename()
        ).bounds(this.width / 2 - 110, 124, 106, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.delete"),
                button -> confirmDelete()
        ).bounds(this.width / 2 + 4, 124, 106, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.create_from_style"),
                button -> createFromStyle()
        ).bounds(this.width / 2 - 110, 154, 220, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.export"),
                button -> exportStyle()
        ).bounds(this.width / 2 - 110, 184, 220, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> onClose()
        ).bounds(this.width / 2 - 110, this.height - 40, 220, 20).build());
        this.setInitialFocus(nameBox);
    }

    private void rename() {
        if (DollLocalStyleStore.rename(style, nameBox.getValue())) {
            reloadAndReturn();
        } else {
            status = Component.translatable("screen.totemdoll.name_required");
        }
    }

    private void confirmDelete() {
        this.minecraft.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        delete();
                    } else {
                        this.minecraft.setScreen(this);
                    }
                },
                Component.translatable("screen.totemdoll.delete_confirm_title"),
                Component.translatable("screen.totemdoll.delete_confirm", style.label())
        ));
    }

    private void delete() {
        if (!DollLocalStyleStore.delete(style)) {
            status = Component.translatable("screen.totemdoll.delete_failed");
            this.minecraft.setScreen(this);
            return;
        }
        TotemDollConfig.select(DollStyles.ALEX_ID);
        reloadAndReturn();
    }

    private void createFromStyle() {
        if (style.templateId() != null) {
            this.minecraft.setScreen(new DollCreateScreen(this, DollStyles.get(style.templateId())));
        }
    }

    private void exportStyle() {
        String path = TinyFileDialogs.tinyfd_saveFileDialog(Component.translatable("screen.totemdoll.export").getString(), style.id().getPath() + ".zip", null, "Totem Doll style pack (*.zip)");
        if (path == null) return;
        try { DollLocalStyleStore.exportStyle(style, java.nio.file.Path.of(path)); status = Component.translatable("screen.totemdoll.export_success"); }
        catch (Exception exception) { status = Component.translatable("screen.totemdoll.pack_import_failed", exception.getMessage()); }
    }

    private void reloadAndReturn() {
        status = Component.translatable("screen.totemdoll.loading");
        Screen rootParent = rootParent();
        TotemDollClient.reloadGeneratedStyles().thenRun(() -> this.minecraft.execute(() ->
                this.minecraft.setScreen(new DollSelectionScreen(rootParent, DollSelectionScreen.Tab.MY_STYLES))
        ));
    }

    @Override
    public Screen rootParent() {
        return parent instanceof DollScreenParent dollParent ? dollParent.rootParent() : parent;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 22, 0xFFFFFF);
        graphics.drawCenteredString(this.font, style.label(), this.width / 2, 44, 0xA0A0A0);
        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2.0F, 52.0F, 100.0F);
        graphics.pose().scale(2.8F, 2.8F, 2.8F);
        DollPreviewContext.renderAs(
                style,
                () -> graphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING), -8, 0)
        );
        graphics.pose().popPose();
        if (status != null) {
            graphics.drawCenteredString(this.font, status, this.width / 2, this.height - 64, 0xFF8080);
        }
        DollScreenRender.renderChildren(this, graphics, mouseX, mouseY, partialTick);
    }
}
