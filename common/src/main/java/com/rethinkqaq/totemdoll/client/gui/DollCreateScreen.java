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
import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

//? >= 1.21.3 {
/*import net.minecraft.client.renderer.RenderType;
*///?}

import java.io.IOException;
import java.nio.file.Path;

public final class DollCreateScreen extends Screen {

    private final Screen parent;
    private final DollStyle template;
    private EditBox nameBox;
    private Path skinPath;
    private ResourceLocation previewTexture;
    private Component status;

    public DollCreateScreen(Screen parent, DollStyle template) {
        super(Component.translatable("screen.totemdoll.create_title"));
        this.parent = parent;
        this.template = template;
    }

    @Override
    protected void init() {
        nameBox = new EditBox(
                this.font,
                this.width / 2 - 110,
                112,
                220,
                20,
                Component.translatable("screen.totemdoll.name")
        );
        nameBox.setMaxLength(48);
        this.addRenderableWidget(nameBox);
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.choose_skin"),
                button -> chooseSkin()
        ).bounds(this.width / 2 - 110, 142, 220, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.totemdoll.create_confirm"),
                button -> create()
        ).bounds(this.width / 2 - 110, this.height - 48, 106, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> onClose()
        ).bounds(this.width / 2 + 4, this.height - 48, 106, 20).build());
        this.setInitialFocus(nameBox);
    }

    private void chooseSkin() {
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
        Path selected = Path.of(chosen);
        try {
            DollLocalStyleStore.validateSkinFile(selected);
            skinPath = selected;
            releasePreviewTexture();
            try (var input = java.nio.file.Files.newInputStream(selected)) {
                DynamicTexture texture = new DynamicTexture(
                        //? >= 1.21.5 {
                        /*() -> "totemdoll_skin_preview",
                        *///?}
                        NativeImage.read(input)
                );

                //? >= 1.21.4 {
                /*previewTexture = ResourceLocation.fromNamespaceAndPath(
                        "totemdoll",
                        "dynamic/skin_preview"
                );
                this.minecraft.getTextureManager().register(previewTexture, texture);
                *///?} else {
                previewTexture = this.minecraft.getTextureManager().register(
                        "totemdoll_skin_preview",
                        texture
                );
                //?}
            }
            if (nameBox.getValue().isBlank()) {
                String fileName = selected.getFileName().toString();
                int extension = fileName.lastIndexOf('.');
                nameBox.setValue(extension > 0 ? fileName.substring(0, extension) : fileName);
            }
            status = Component.translatable("screen.totemdoll.skin_selected", selected.getFileName().toString());
        } catch (IOException exception) {
            status = Component.translatable("screen.totemdoll.import_failed", exception.getMessage());
        }
    }

    private void create() {
        if (this.minecraft == null || skinPath == null) {
            status = Component.translatable("screen.totemdoll.skin_required");
            return;
        }
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) {
            status = Component.translatable("screen.totemdoll.name_required");
            return;
        }
        try {
            var createdId = DollLocalStyleStore.importSkin(
                    template,
                    skinPath,
                    name,
                    this.minecraft.getResourceManager()
            );
            status = Component.translatable("screen.totemdoll.loading");
            Screen rootParent = parent instanceof DollScreenParent dollParent
                    ? dollParent.rootParent()
                    : parent;
            TotemDollClient.reloadGeneratedStyles().thenRun(() -> this.minecraft.execute(() -> {
                TotemDollConfig.select(createdId);
                this.minecraft.setScreen(new DollSelectionScreen(rootParent, DollSelectionScreen.Tab.MY_STYLES));
            }));
        } catch (IOException exception) {
            status = Component.translatable("screen.totemdoll.import_failed", exception.getMessage());
        }
    }

    @Override
    public void onClose() {
        releasePreviewTexture();
        this.minecraft.setScreen(parent);
    }

    @Override
    public void removed() {
        releasePreviewTexture();
        super.removed();
    }

    private void releasePreviewTexture() {
        if (previewTexture != null && this.minecraft != null) {
            this.minecraft.getTextureManager().release(previewTexture);
            previewTexture = null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.totemdoll.from_template", template.label()),
                this.width / 2,
                38,
                0xA0A0A0
        );
        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2.0F, 52.0F, 100.0F);
        graphics.pose().scale(3.2F, 3.2F, 3.2F);
        DollPreviewContext.renderAs(
                template,
                () -> graphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING), -8, 0)
        );
        graphics.pose().popPose();
        graphics.drawString(this.font, Component.translatable("screen.totemdoll.name"), this.width / 2 - 110, 100, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                skinPath == null
                        ? Component.translatable("screen.totemdoll.no_skin")
                        : Component.translatable("screen.totemdoll.skin_selected", skinPath.getFileName().toString()),
                this.width / 2,
                172,
                skinPath == null ? 0xA0A0A0 : 0x80C080
        );
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.totemdoll.skin_format_hint"),
                this.width / 2,
                190,
                0x808080
        );
        if (previewTexture != null) {
            int previewX = Math.min(this.width - 72, this.width / 2 + 140);
            int previewCenter = previewX + 32;
            graphics.blit(/*? >= 1.21.3 {*//*RenderType::guiTextured, *//*?}*/previewTexture, previewX, 104, 0, 0, 64, 64, 64, 64);
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("screen.totemdoll.skin_preview"),
                    previewCenter,
                    172,
                    0xA0A0A0
            );
        }
        if (status != null) {
            graphics.drawCenteredString(this.font, status, this.width / 2, this.height - 70, 0xFF8080);
        }
        DollScreenRender.renderChildren(this, graphics, mouseX, mouseY, partialTick);
    }
}
