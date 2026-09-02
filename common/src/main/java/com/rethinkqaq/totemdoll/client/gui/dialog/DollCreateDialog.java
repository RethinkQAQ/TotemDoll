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
 */

package com.rethinkqaq.totemdoll.client.gui.dialog;

import com.rethinkqaq.totemdoll.client.gui.DollSelectionScreen;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBinding;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.minecraft.MinecraftPreview;
import com.rethinkqaq.configui.core.component.UiButton;
import com.rethinkqaq.configui.core.component.input.UiTextField;
import com.rethinkqaq.totemdoll.Constants;
import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.config.TotemDollConfigRuntime;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.doll.DollStyle;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
//? >= 1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}
import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.List;

/** RCUI form for creating a local style; file selection remains a host operation. */
public final class DollCreateDialog extends Ui.Node implements Ui.ChildProvider {
    private final UiDialogHost dialogs;
    private final DollSelectionScreen owner;
    private final DollStyle template;
    private final String[] draft = {""};
    private final UiTextField name;
    private final MinecraftPreview skinPreview;
    private final UiButton chooseSkin;
    private final UiButton create;
    private final UiButton done;
    private Path skinPath;
    //? >= 1.21.11 {
    /*private Identifier previewTexture;
    *///?} else {
    private ResourceLocation previewTexture;
    //?}
    private DynamicTexture previewDynamicTexture;
    private UiText status;

    public DollCreateDialog(UiDialogHost dialogs, DollSelectionScreen owner, DollStyle template) {
        this.dialogs = dialogs;
        this.owner = owner;
        this.template = template;
        name = Ui.textField(UiBinding.of(() -> draft[0], value -> draft[0] = value))
                .placeholder(UiText.translatable("screen.totemdoll.style_name")).maxLength(48);
        skinPreview = new MinecraftPreview(this::renderSkinPreview).preferredHeight(76);
        chooseSkin = Ui.button(UiText.translatable("screen.totemdoll.choose_skin"), this::chooseSkin)
                .variant(Ui.ButtonVariant.SECONDARY);
        create = Ui.button(UiText.translatable("screen.totemdoll.create"), this::create)
                .variant(Ui.ButtonVariant.PRIMARY);
        done = Ui.button(UiText.translatable("gui.done"), dialogs::close)
                .variant(Ui.ButtonVariant.SECONDARY);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding();
        float width = Math.min(maxWidth, 380);
        float inner = Math.max(0, width - padding * 2);
        float gap = theme.metrics().spacing();
        name.measure(renderer, inner, maxHeight, theme);
        skinPreview.measure(renderer, inner, skinPath == null ? 0 : maxHeight, theme);
        chooseSkin.measure(renderer, inner, maxHeight, theme);
        create.measure(renderer, inner, maxHeight, theme);
        done.measure(renderer, inner, maxHeight, theme);
        float statusHeight = status == null ? 0 : renderer.lineHeight() + gap;
        measuredWidth = width;
        measuredHeight = Math.min(maxHeight, padding * 2 + renderer.lineHeight() + gap
                + name.measuredHeight() + gap + skinPreview.measuredHeight() + gap
                + chooseSkin.measuredHeight() + statusHeight
                + gap + Math.max(create.measuredHeight(), done.measuredHeight()));
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float padding = theme.metrics().padding();
        float gap = theme.metrics().spacing();
        float x = value.x() + padding;
        float width = Math.max(0, value.width() - padding * 2);
        float y = value.y() + padding + renderer.lineHeight() + gap;
        name.layout(renderer, new UiBounds(x, y, width, name.measuredHeight()), theme);
        y += name.measuredHeight() + gap;
        skinPreview.layout(renderer, new UiBounds(x, y, width, skinPreview.measuredHeight()), theme);
        y += skinPreview.measuredHeight() + gap;
        chooseSkin.layout(renderer, new UiBounds(x, y, width, chooseSkin.measuredHeight()), theme);
        y += chooseSkin.measuredHeight() + gap;
        if (status != null) y += renderer.lineHeight() + gap;
        float row = Math.max(create.measuredHeight(), done.measuredHeight());
        create.layout(renderer, new UiBounds(x, value.y() + value.height() - padding - row,
                Math.max(0, (width - gap) / 2f), row), theme);
        done.layout(renderer, new UiBounds(x + Math.max(0, (width - gap) / 2f) + gap,
                value.y() + value.height() - padding - row,
                Math.max(0, (width - gap) / 2f), row), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().surfaceRaised());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        float padding = theme.metrics().padding();
        renderer.drawText(UiText.translatable("screen.totemdoll.create_from_title", template.label().getString()),
                bounds.x() + padding, bounds.y() + padding, theme.palette().textPrimary());
        name.render(renderer, theme);
        skinPreview.render(renderer, theme);
        chooseSkin.render(renderer, theme);
        if (status != null) Ui.drawFittedText(renderer, status, bounds.x() + padding,
                chooseSkin.bounds().y() + chooseSkin.bounds().height() + theme.metrics().spacing() / 2f,
                Math.max(0, bounds.width() - padding * 2), theme.palette().textSecondary());
        create.render(renderer, theme);
        done.render(renderer, theme);
    }

    private void chooseSkin() {
        String chosen = TinyFileDialogs.tinyfd_openFileDialog("Select a 64x64 Minecraft skin", "", null,
                "PNG image", false);
        if (chosen == null) return;
        Path selected = Path.of(chosen);
        try {
            DollLocalStyleStore.validateSkinFile(selected);
            skinPath = selected;
            loadPreviewTexture(selected);
            if (draft[0].isBlank()) {
                String fileName = selected.getFileName().toString();
                int extension = fileName.lastIndexOf('.');
                draft[0] = extension > 0 ? fileName.substring(0, extension) : fileName;
                name.cancel();
            }
            status = UiText.translatable("screen.totemdoll.status.skin_selected", selected.getFileName().toString());
        } catch (Exception exception) {
            status = UiText.translatable("screen.totemdoll.status.invalid_skin", exception.getMessage());
            Constants.LOG.warn("Could not validate Totem Doll skin {}", selected, exception);
        }
        invalidateLayout();
    }

    private void loadPreviewTexture(Path selected) throws java.io.IOException {
        releasePreviewTexture();
        try (var input = java.nio.file.Files.newInputStream(selected)) {
            previewDynamicTexture = new DynamicTexture(
                    //? >= 1.21.5 {
                    /*() -> "totemdoll_skin_preview",
                    *///?}
                    NativeImage.read(input));
        }
        //? >= 1.21.4 {
        /*previewTexture = DollMinecraftResourceUtil.nativeId(
                DollResourceId.of("totemdoll", "dynamic/skin_preview"));
        Minecraft.getInstance().getTextureManager().register(previewTexture, previewDynamicTexture);
        *///?} else {
        previewTexture = Minecraft.getInstance().getTextureManager().register(
                "totemdoll_skin_preview", previewDynamicTexture);
        //?}
    }

    private void renderSkinPreview(
            //? >= 26.1 {
            /*net.minecraft.client.gui.GuiGraphicsExtractor graphics
            *///?} else {
            net.minecraft.client.gui.GuiGraphics graphics
            //?}
            , UiBounds area, UiBounds ignoredClip) {
        if (previewTexture == null) return;
        var gui = com.rethinkqaq.totemdoll.utils.DollGuiGraphics.wrap(graphics);
        int size = Math.max(1, Math.round(Math.min(area.width(), area.height())));
        int x = Math.round(area.x() + (area.width() - size) / 2f);
        int y = Math.round(area.y() + (area.height() - size) / 2f);
        gui.blitTexture(previewTexture, x, y, 0, 0, size, size, 64, 64);
    }

    private void releasePreviewTexture() {
        if (previewTexture != null) {
            Minecraft.getInstance().getTextureManager().release(previewTexture);
            previewTexture = null;
        }
        previewDynamicTexture = null;
    }

    private void create() {
        if (skinPath == null) {
            status = UiText.translatable("screen.totemdoll.status.skin_required");
            invalidateLayout();
            return;
        }
        String displayName = draft[0] == null ? "" : draft[0].trim();
        if (displayName.isEmpty()) {
            status = UiText.translatable("screen.totemdoll.status.name_required");
            invalidateLayout();
            return;
        }
        try {
            var id = DollLocalStyleStore.importSkin(template, skinPath, displayName,
                    Minecraft.getInstance().getResourceManager());
            TotemDollConfigRuntime.select(id);
            status = UiText.translatable("screen.totemdoll.status.creating");
            TotemDollClient.reloadGeneratedStyles().thenRun(() -> Minecraft.getInstance().execute(() -> {
                dialogs.close();
                owner.reloadStyles(DollSelectionScreen.Tab.MY_STYLES);
            }));
        } catch (Exception exception) {
            status = UiText.translatable("screen.totemdoll.status.creation_failed", exception.getMessage());
            Constants.LOG.warn("Could not create Totem Doll style", exception);
            invalidateLayout();
        }
    }

    @Override public List<Ui.Node> childNodes() { return List.of(name, skinPreview, chooseSkin, create, done); }
    @Override public boolean click(float x, float y, int button) {
        return done.click(x, y, button) || create.click(x, y, button)
                || chooseSkin.click(x, y, button) || name.click(x, y, button);
    }
    @Override public boolean drag(float x, float y, int button) { return name.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) {
        return done.release(x, y, button) || create.release(x, y, button)
                || chooseSkin.release(x, y, button) || name.release(x, y, button);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        return done.key(event, clipboard) || create.key(event, clipboard)
                || chooseSkin.key(event, clipboard) || name.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        return name.textInput(event, clipboard);
    }
}
