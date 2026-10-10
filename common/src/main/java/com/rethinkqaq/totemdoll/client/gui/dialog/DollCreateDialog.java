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
import com.rethinkqaq.totemdoll.utils.DollFileDialog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionException;

/** RCUI form for creating a local style; file selection remains a host operation. */
public final class DollCreateDialog extends Ui.Node implements Ui.ChildProvider {
    private final UiDialogHost dialogs;
    private final DollSelectionScreen owner;
    private final DollStyle template;
    private final String[] draft = {""};
    private final String[] playerDraft = {""};
    private final UiTextField name;
    private final UiTextField playerId;
    private final MinecraftPreview skinPreview;
    private final UiButton chooseSkin;
    private final UiButton fetchSkinButton;
    private final UiButton create;
    private final UiButton done;
    private Path skinPath;
    private Path downloadedSkinPath;
    private long fetchRequest;
    private boolean fetching;
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
        playerId = Ui.textField(UiBinding.of(() -> playerDraft[0], value -> playerDraft[0] = value))
                .placeholder(UiText.translatable("screen.totemdoll.player_id"))
                .maxLength(16)
                .onSubmit(this::fetchSkin);
        skinPreview = new MinecraftPreview(this::renderSkinPreview).preferredHeight(76);
        chooseSkin = Ui.button(UiText.translatable("screen.totemdoll.choose_skin"), this::chooseSkin)
                .variant(Ui.ButtonVariant.SECONDARY);
        fetchSkinButton = Ui.button(UiText.translatable("screen.totemdoll.fetch_skin"), this::fetchSkin)
                .variant(Ui.ButtonVariant.SECONDARY)
                .preferredWidth(110);
        create = Ui.button(UiText.translatable("screen.totemdoll.create"), this::create)
                .variant(Ui.ButtonVariant.PRIMARY);
        done = Ui.button(UiText.translatable("gui.done"), this::close)
                .variant(Ui.ButtonVariant.SECONDARY);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding();
        float width = Math.min(maxWidth, 380);
        float inner = Math.max(0, width - padding * 2);
        float gap = theme.metrics().spacing();
        name.measure(renderer, inner, maxHeight, theme);
        playerId.measure(renderer, inner, maxHeight, theme);
        fetchSkinButton.measure(renderer, inner, maxHeight, theme);
        skinPreview.measure(renderer, inner, skinPath == null ? 0 : maxHeight, theme);
        chooseSkin.measure(renderer, inner, maxHeight, theme);
        create.measure(renderer, inner, maxHeight, theme);
        done.measure(renderer, inner, maxHeight, theme);
        float statusHeight = status == null ? 0 : renderer.lineHeight() + gap;
        measuredWidth = width;
        float skinSourceHeight = Math.max(playerId.measuredHeight(), fetchSkinButton.measuredHeight());
        measuredHeight = Math.min(maxHeight, padding * 2 + renderer.lineHeight() + gap
                + name.measuredHeight() + gap + skinSourceHeight + gap
                + skinPreview.measuredHeight() + gap
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
        float skinSourceHeight = Math.max(playerId.measuredHeight(), fetchSkinButton.measuredHeight());
        float fetchWidth = Math.min(width, Math.max(110, fetchSkinButton.measuredWidth()));
        playerId.layout(renderer, new UiBounds(x, y, Math.max(0, width - fetchWidth - gap), skinSourceHeight), theme);
        fetchSkinButton.layout(renderer, new UiBounds(x + Math.max(0, width - fetchWidth), y,
                fetchWidth, skinSourceHeight), theme);
        y += skinSourceHeight + gap;
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
        playerId.render(renderer, theme);
        fetchSkinButton.render(renderer, theme);
        skinPreview.render(renderer, theme);
        chooseSkin.render(renderer, theme);
        if (status != null) Ui.drawFittedText(renderer, status, bounds.x() + padding,
                chooseSkin.bounds().y() + chooseSkin.bounds().height() + theme.metrics().spacing() / 2f,
                Math.max(0, bounds.width() - padding * 2), theme.palette().textSecondary());
        create.render(renderer, theme);
        done.render(renderer, theme);
    }

    private void chooseSkin() {
        ++fetchRequest;
        fetching = false;
        Path selected = DollFileDialog.open("Select a 64x64 Minecraft skin", "PNG image", "png");
        if (selected == null) return;
        try {
            DollLocalStyleStore.validateSkinFile(selected);
            releasePreviewTexture();
            deleteDownloadedSkin();
            skinPath = selected;
            loadPreviewTexture(selected);
            if (name.draft().isBlank()) {
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

    private void fetchSkin() {
        if (fetching) return;
        String playerName = playerId.draft() == null ? "" : playerId.draft().trim();
        if (playerName.isEmpty()) {
            status = UiText.translatable("screen.totemdoll.status.player_id_required");
            invalidateLayout();
            return;
        }

        fetching = true;
        long request = ++fetchRequest;
        status = UiText.translatable("screen.totemdoll.status.fetching_skin", playerName);
        invalidateLayout();
        DollSkinFetcher.fetch(playerName).whenComplete((bytes, failure) -> Minecraft.getInstance().execute(() -> {
            if (request != fetchRequest) return;
            fetching = false;
            if (failure != null) {
                status = fetchFailureText(failure);
                Constants.LOG.warn("Could not fetch the Minecraft skin for {}", playerName, unwrap(failure));
                invalidateLayout();
                return;
            }
            Path downloaded = null;
            try {
                downloaded = Files.createTempFile("totemdoll-skin-", ".png");
                Files.write(downloaded, bytes);
                downloaded.toFile().deleteOnExit();
                DollLocalStyleStore.validateSkinFile(downloaded);
                releasePreviewTexture();
                deleteDownloadedSkin();
                downloadedSkinPath = downloaded;
                skinPath = downloaded;
                loadPreviewTexture(downloaded);
                if (name.draft().isBlank()) {
                    draft[0] = playerName;
                    name.cancel();
                }
                status = UiText.translatable("screen.totemdoll.status.skin_fetched", playerName);
            } catch (Exception exception) {
                if (downloaded != null) {
                    try { Files.deleteIfExists(downloaded); } catch (java.io.IOException ignored) { }
                }
                status = UiText.translatable("screen.totemdoll.status.invalid_skin", exception.getMessage());
                Constants.LOG.warn("Could not prepare the fetched Minecraft skin for {}", playerName, exception);
            }
            invalidateLayout();
        }));
    }

    private UiText fetchFailureText(Throwable failure) {
        Throwable cause = unwrap(failure);
        if (cause instanceof DollSkinFetcher.FetchException exception) {
            return switch (exception.reason()) {
                case INVALID_NAME -> UiText.translatable("screen.totemdoll.status.invalid_player_id");
                case NOT_FOUND -> UiText.translatable("screen.totemdoll.status.player_not_found");
                case NO_SKIN -> UiText.translatable("screen.totemdoll.status.player_no_skin");
                case RATE_LIMITED -> UiText.translatable("screen.totemdoll.status.skin_rate_limited");
                default -> UiText.translatable("screen.totemdoll.status.skin_fetch_failed");
            };
        }
        return UiText.translatable("screen.totemdoll.status.skin_fetch_failed");
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable cause = throwable;
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
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

    private void deleteDownloadedSkin() {
        if (downloadedSkinPath == null) return;
        try {
            Files.deleteIfExists(downloadedSkinPath);
        } catch (java.io.IOException exception) {
            Constants.LOG.debug("Could not delete temporary fetched skin", exception);
        }
        downloadedSkinPath = null;
    }

    private void close() {
        ++fetchRequest;
        fetching = false;
        releasePreviewTexture();
        deleteDownloadedSkin();
        dialogs.close();
    }

    private void create() {
        if (fetching) return;
        name.commit();
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
            deleteDownloadedSkin();
            status = UiText.translatable("screen.totemdoll.status.creating");
            TotemDollClient.reloadGeneratedStyles().thenRun(() -> Minecraft.getInstance().execute(() -> {
                TotemDollConfigRuntime.select(id);
                dialogs.close();
                owner.showStyles(DollSelectionScreen.Tab.MY_STYLES);
            }));
        } catch (Exception exception) {
            status = UiText.translatable("screen.totemdoll.status.creation_failed", exception.getMessage());
            Constants.LOG.warn("Could not create Totem Doll style", exception);
            invalidateLayout();
        }
    }

    @Override public List<Ui.Node> childNodes() {
        return List.of(name, playerId, fetchSkinButton, skinPreview, chooseSkin, create, done);
    }
    @Override public boolean click(float x, float y, int button) {
        return done.click(x, y, button) || create.click(x, y, button)
                || chooseSkin.click(x, y, button) || fetchSkinButton.click(x, y, button)
                || playerId.click(x, y, button) || name.click(x, y, button);
    }
    @Override public boolean drag(float x, float y, int button) { return name.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) {
        return done.release(x, y, button) || create.release(x, y, button)
                || chooseSkin.release(x, y, button) || fetchSkinButton.release(x, y, button)
                || playerId.release(x, y, button) || name.release(x, y, button);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        return done.key(event, clipboard) || create.key(event, clipboard)
                || chooseSkin.key(event, clipboard) || fetchSkinButton.key(event.keyCode())
                || playerId.key(event, clipboard) || name.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) {
        return playerId.textInput(event, clipboard) || name.textInput(event, clipboard);
    }
}
