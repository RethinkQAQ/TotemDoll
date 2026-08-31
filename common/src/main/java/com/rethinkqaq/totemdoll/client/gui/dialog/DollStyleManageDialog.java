/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
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
import com.rethinkqaq.totemdoll.doll.DollStyles;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.List;

/** RCUI management dialog for a user-created style. File and style operations remain host-owned. */
public final class DollStyleManageDialog extends Ui.Node implements Ui.ChildProvider {
    private final UiDialogHost dialogs;
    private final DollSelectionScreen owner;
    private final DollStyle style;
    private final String[] draft = {""};
    private final UiTextField name;
    private final MinecraftPreview preview;
    private final UiButton rename;
    private final UiButton delete;
    private final UiButton create;
    private final Ui.Node createTooltip;
    private final UiButton export;
    private final UiButton done;
    private UiText status;

    private boolean canModifyStyle() {
        // The generated resource pack may change the reported origin while the
        // style still points at the user's local style directory. userCreated
        // is the persisted ownership bit and is the only stable edit predicate.
        return style.isLocal() && style.origin() != com.rethinkqaq.totemdoll.doll.DollStyleOrigin.BUILTIN;
    }

    public DollStyleManageDialog(UiDialogHost dialogs, DollSelectionScreen owner, DollStyle style) {
        this.dialogs = dialogs;
        this.owner = owner;
        this.style = style;
        draft[0] = style.displayName();
        name = Ui.textField(UiBinding.of(() -> draft[0], value -> draft[0] = value))
                .placeholder(UiText.translatable("screen.totemdoll.style_name"))
                .maxLength(48);
        preview = new MinecraftPreview((graphics, area, clip) -> {
            com.rethinkqaq.totemdoll.client.gui.preview.DollGuiPreview.render(
                    com.rethinkqaq.totemdoll.utils.DollGuiGraphics.wrap(graphics), style,
                    Math.round(area.x()), Math.round(area.y()), Math.round(area.width()), Math.round(area.height()),
                    Math.min(area.width(), area.height()) * .88f,
                    Math.round(clip.x()), Math.round(clip.y()), Math.round(clip.width()), Math.round(clip.height()));
        }).preferredHeight(112);
        rename = Ui.button(UiText.translatable("screen.totemdoll.rename"), this::rename).variant(Ui.ButtonVariant.PRIMARY);
        rename.enabled(canModifyStyle());
        delete = Ui.button(UiText.translatable("screen.totemdoll.delete"), this::confirmDelete).variant(Ui.ButtonVariant.DANGER);
        create = Ui.button(UiText.translatable("screen.totemdoll.create_from_style"), this::createFromStyle).variant(Ui.ButtonVariant.SECONDARY);
        boolean canCreate = style.skin() != null && style.skin().supportsImport() && style.templateId() != null;
        create.enabled(canCreate);
        createTooltip = canCreate ? create : Ui.tooltip(create,
                UiText.translatable("screen.totemdoll.create_from_style_unavailable"));
        export = Ui.button(UiText.translatable("screen.totemdoll.export"), this::exportStyle).variant(Ui.ButtonVariant.SECONDARY);
        export.enabled(canModifyStyle());
        done = Ui.button(UiText.translatable("gui.done"), dialogs::close).variant(Ui.ButtonVariant.PRIMARY);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding();
        float width = Math.min(maxWidth, 380);
        float inner = Math.max(0, width - padding * 2);
        float gap = theme.metrics().spacing();
        name.measure(renderer, inner, maxHeight, theme);
        preview.measure(renderer, inner, maxHeight, theme);
        rename.measure(renderer, inner, maxHeight, theme);
        delete.measure(renderer, inner, maxHeight, theme);
        createTooltip.measure(renderer, inner, maxHeight, theme);
        export.measure(renderer, inner, maxHeight, theme);
        done.measure(renderer, inner, maxHeight, theme);
        float row1 = Math.max(rename.measuredHeight(), delete.measuredHeight());
        measuredWidth = width;
        measuredHeight = Math.min(maxHeight, padding * 2 + renderer.lineHeight() + gap
                + preview.measuredHeight() + gap + name.measuredHeight() + gap + row1 + gap
                + createTooltip.measuredHeight() + gap + export.measuredHeight() + gap * 2 + done.measuredHeight());
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float padding = theme.metrics().padding();
        float gap = theme.metrics().spacing();
        float x = value.x() + padding;
        float width = Math.max(0, value.width() - padding * 2);
        float y = value.y() + padding + renderer.lineHeight() + gap;
        preview.layout(renderer, new UiBounds(x, y, width, preview.measuredHeight()), theme);
        y += preview.measuredHeight() + gap;
        name.layout(renderer, new UiBounds(x, y, width, name.measuredHeight()), theme);
        y += name.measuredHeight() + gap;
        float half = Math.max(0, (width - gap) / 2f);
        float row = Math.max(rename.measuredHeight(), delete.measuredHeight());
        rename.layout(renderer, new UiBounds(x, y, half, row), theme);
        delete.layout(renderer, new UiBounds(x + half + gap, y, half, row), theme);
        y += row + gap;
        row = createTooltip.measuredHeight();
        createTooltip.layout(renderer, new UiBounds(x, y, width, row), theme);
        y += row + gap;
        row = export.measuredHeight();
        export.layout(renderer, new UiBounds(x, y, width, row), theme);
        done.layout(renderer, new UiBounds(x + width - done.measuredWidth(),
                value.y() + value.height() - padding - done.measuredHeight(), done.measuredWidth(), done.measuredHeight()), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().surfaceRaised());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        float padding = theme.metrics().padding();
        renderer.drawCenteredText(UiText.translatable("screen.totemdoll.manage_style"), bounds.x() + bounds.width() / 2f,
                bounds.y() + padding, theme.palette().textPrimary());
        preview.render(renderer, theme);
        name.render(renderer, theme);
        rename.render(renderer, theme);
        delete.render(renderer, theme);
        createTooltip.render(renderer, theme);
        export.render(renderer, theme);
        if (status != null) Ui.drawFittedText(renderer, status, bounds.x() + padding,
                export.bounds().y() + export.bounds().height() + theme.metrics().spacing() / 2f,
                Math.max(0, bounds.width() - padding * 2), theme.palette().danger());
        done.render(renderer, theme);
    }

    private void rename() {
        if (!canModifyStyle()) {
            status = UiText.translatable("screen.totemdoll.status.rename_failed");
            invalidateLayout();
            return;
        }
        String value = draft[0] == null ? "" : draft[0].trim();
        if (value.isEmpty()) {
            status = UiText.translatable("screen.totemdoll.status.name_required");
            invalidateLayout();
            return;
        }
        if (!DollLocalStyleStore.rename(style, value)) {
            status = UiText.translatable("screen.totemdoll.status.rename_failed");
            invalidateLayout();
            return;
        }
        dialogs.close();
        owner.reloadStyles(DollSelectionScreen.Tab.MY_STYLES);
    }

    private void confirmDelete() {
        dialogs.show(new DollConfirmDialog(dialogs,
                UiText.translatable("screen.totemdoll.delete_confirm_title"),
                UiText.translatable("screen.totemdoll.delete_confirm", style.label().getString()), this::delete));
    }

    private void delete() {
        if (!DollLocalStyleStore.delete(style)) {
            status = UiText.translatable("screen.totemdoll.status.delete_failed");
            dialogs.show(this);
            return;
        }
        TotemDollConfigRuntime.select(DollStyles.ALEX_ID);
        dialogs.close();
        owner.reloadStyles(DollSelectionScreen.Tab.MY_STYLES);
    }

    private void createFromStyle() {
        if (style.skin() == null || !style.skin().supportsImport() || style.templateId() == null) return;
        dialogs.show(new DollCreateDialog(dialogs, owner, DollStyles.get(style.templateId())));
    }

    private void exportStyle() {
        if (!canModifyStyle()) {
            status = UiText.translatable("screen.totemdoll.status.export_failed");
            invalidateLayout();
            return;
        }
        String path = TinyFileDialogs.tinyfd_saveFileDialog("Export style", style.id().path() + ".zip", null,
                "Totem Doll style pack (*.zip)");
        if (path == null) return;
        try {
            DollLocalStyleStore.exportStyle(style, Path.of(path));
            status = UiText.translatable("screen.totemdoll.status.exported");
        } catch (Exception exception) {
            Constants.LOG.warn("Could not export Totem Doll style {}", style.id(), exception);
            status = UiText.translatable("screen.totemdoll.status.export_failed");
        }
        invalidateLayout();
    }

    @Override public List<Ui.Node> childNodes() { return List.of(preview, name, rename, delete, createTooltip, export, done); }
    @Override public boolean click(float x, float y, int button) {
        return done.click(x, y, button) || export.click(x, y, button) || createTooltip.click(x, y, button)
                || delete.click(x, y, button) || rename.click(x, y, button) || name.click(x, y, button);
    }
    @Override public boolean drag(float x, float y, int button) { return name.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) {
        return done.release(x, y, button) | export.release(x, y, button) | createTooltip.release(x, y, button)
                | delete.release(x, y, button) | rename.release(x, y, button) | name.release(x, y, button);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        return done.key(event, clipboard) || export.key(event, clipboard) || createTooltip.key(event, clipboard)
                || delete.key(event, clipboard) || rename.key(event, clipboard) || name.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return name.textInput(event, clipboard); }
}
