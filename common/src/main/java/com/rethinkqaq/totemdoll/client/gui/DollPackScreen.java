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

import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.nio.file.Path;

public final class DollPackScreen extends Screen implements DollScreenParent {
    private final Screen parent;
    private Component status;
    public DollPackScreen(Screen parent) { super(Component.translatable("screen.totemdoll.pack_title")); this.parent = parent; }

    public static void chooseZipAndImport(Screen parent) {
        String file = TinyFileDialogs.tinyfd_openFileDialog(
                Component.translatable("screen.totemdoll.import_zip").getString(), "", null,
                "Totem Doll style pack (*.zip)", false);
        if (file == null) return;
        try {
            DollLocalStyleStore.importZip(Path.of(file));
            TotemDollClient.reloadGeneratedStyles().thenRun(() -> Minecraft.getInstance().execute(() ->
                    Minecraft.getInstance().setScreen(new DollSelectionScreen(parent, DollSelectionScreen.Tab.TEMPLATES))));
        } catch (Exception exception) {
            Constants.LOG.warn("Could not import style pack {}", file, exception);
        }
    }
    @Override protected void init() {
        int center = width / 2;
        addRenderableWidget(Button.builder(Component.translatable("screen.totemdoll.import_zip"), b -> chooseZip()).bounds(center - 100, 52, 200, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose()).bounds(center - 100, height - 32, 200, 20).build());
    }
    private void chooseZip() { String file = TinyFileDialogs.tinyfd_openFileDialog(title.getString(), "", null, "ZIP style pack", false); if (file != null) importPack(Path.of(file), true); }
    private void importPack(Path path, boolean zip) {
        try { DollLocalStyleStore.importZip(path); status = Component.translatable("screen.totemdoll.import_success"); TotemDollClient.reloadGeneratedStyles().thenRun(() -> minecraft.execute(() -> minecraft.setScreen(new DollSelectionScreen(parent, DollSelectionScreen.Tab.TEMPLATES)))); }
        catch (Exception exception) { status = Component.translatable("screen.totemdoll.pack_import_failed", exception.getMessage()); }
    }
    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public Screen rootParent() { return parent instanceof DollScreenParent p ? p.rootParent() : parent; }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { renderTransparentBackground(graphics); graphics.drawCenteredString(font, title, width / 2, 22, 0xFFFFFF); graphics.drawCenteredString(font, Component.translatable("screen.totemdoll.import_hint"), width / 2, 84, 0xA0A0A0); if (status != null) graphics.drawCenteredString(font, status, width / 2, height - 54, 0xE0C070); DollScreenRender.renderChildren(this, graphics, mouseX, mouseY, partialTick); }
}
