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

import com.rethinkqaq.totemdoll.utils.DollGuiGraphics;
import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import com.rethinkqaq.totemdoll.doll.DollStyles;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class DollStyleManageScreen extends DollScreen implements DollScreenParent {

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
        Button renameButton = Button.builder(
                Component.translatable("screen.totemdoll.rename"),
                button -> rename()
        ).bounds(this.width / 2 - 110, 124, 106, 20).build();
        renameButton.active = style.userCreated();
        this.addRenderableWidget(renameButton);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.delete"),
                button -> confirmDelete()
        ).bounds(this.width / 2 + 4, 124, 106, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.create_from_style"),
                button -> createFromStyle()
        ).bounds(this.width / 2 - 110, 154, 220, 20).build());
        Button exportButton = Button.builder(
                Component.translatable("screen.totemdoll.export"),
                button -> exportStyle()
        ).bounds(this.width / 2 - 110, 184, 220, 20).build();
        exportButton.active = style.userCreated();
        this.addRenderableWidget(exportButton);
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
        DollScreenAdapter.setScreen(this.minecraft, new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        delete();
                    } else {
                        DollScreenAdapter.setScreen(this.minecraft, this);
                    }
                },
                Component.translatable("screen.totemdoll.delete_confirm_title"),
                Component.translatable("screen.totemdoll.delete_confirm", style.label())
        ));
    }

    private void delete() {
        if (!DollLocalStyleStore.delete(style)) {
            status = Component.translatable("screen.totemdoll.delete_failed");
            DollScreenAdapter.setScreen(this.minecraft, this);
            return;
        }
        TotemDollConfig.select(DollStyles.ALEX_ID);
        reloadAndReturn();
    }

    private void createFromStyle() {
        if (style.templateId() != null) {
            DollScreenAdapter.setScreen(this.minecraft, new DollCreateScreen(this, DollStyles.get(style.templateId())));
        }
    }

    private void exportStyle() {
        String path = TinyFileDialogs.tinyfd_saveFileDialog(Component.translatable("screen.totemdoll.export").getString(), style.id().path() + ".zip", null, "Totem Doll style pack (*.zip)");
        if (path == null) return;
        try { DollLocalStyleStore.exportStyle(style, java.nio.file.Path.of(path)); status = Component.translatable("screen.totemdoll.export_success"); }
        catch (Exception exception) { status = Component.translatable("screen.totemdoll.pack_import_failed", exception.getMessage()); }
    }

    private void reloadAndReturn() {
        status = Component.translatable("screen.totemdoll.loading");
        Screen rootParent = rootParent();
        TotemDollClient.reloadGeneratedStyles().thenRun(() -> this.minecraft.execute(() ->
                DollScreenAdapter.setScreen(this.minecraft, new DollSelectionScreen(rootParent, DollSelectionScreen.Tab.MY_STYLES))
        ));
    }

    @Override
    public Screen rootParent() {
        return parent instanceof DollScreenParent dollParent ? dollParent.rootParent() : parent;
    }

    @Override
    public void onClose() {
        DollScreenAdapter.setScreen(this.minecraft, parent);
    }

    @Override
    protected void renderContent(DollGuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.centeredText(this.font, this.title, this.width / 2, 22, 0xFFFFFFFF);
        gui.centeredText(this.font, style.label(), this.width / 2, 44, 0xFFA0A0A0);
        DollGuiPreview.render(gui, style, this.width / 2 - 24, 48, 48, 48, 44.8F);
        if (status != null) {
            gui.centeredText(this.font, status, this.width / 2, this.height - 64, 0xFFFF8080);
        }
        renderChildren(mouseX, mouseY, partialTick);
    }
}
