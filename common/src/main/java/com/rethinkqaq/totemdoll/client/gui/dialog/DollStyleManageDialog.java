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
    private final UiButton rename;
    private final UiButton delete;
    private final UiButton create;
    private final UiButton export;
    private final UiButton done;
    private UiText status;

    public DollStyleManageDialog(UiDialogHost dialogs, DollSelectionScreen owner, DollStyle style) {
        this.dialogs = dialogs;
        this.owner = owner;
        this.style = style;
        draft[0] = style.displayName();
        name = Ui.textField(UiBinding.of(() -> draft[0], value -> draft[0] = value))
                .placeholder(UiText.translatable("screen.totemdoll.style_name"))
                .maxLength(48);
        rename = Ui.button(UiText.translatable("screen.totemdoll.rename"), this::rename).variant(Ui.ButtonVariant.PRIMARY);
        rename.enabled(style.userCreated());
        delete = Ui.button(UiText.translatable("screen.totemdoll.delete"), this::confirmDelete).variant(Ui.ButtonVariant.DANGER);
        create = Ui.button(UiText.translatable("screen.totemdoll.create_from_style"), this::createFromStyle).variant(Ui.ButtonVariant.SECONDARY);
        export = Ui.button(UiText.translatable("screen.totemdoll.export"), this::exportStyle).variant(Ui.ButtonVariant.SECONDARY);
        export.enabled(style.userCreated());
        done = Ui.button(UiText.translatable("gui.done"), dialogs::close).variant(Ui.ButtonVariant.PRIMARY);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding();
        float width = Math.min(maxWidth, 520);
        float inner = Math.max(0, width - padding * 2);
        float gap = theme.metrics().spacing();
        name.measure(renderer, inner, maxHeight, theme);
        rename.measure(renderer, inner, maxHeight, theme);
        delete.measure(renderer, inner, maxHeight, theme);
        create.measure(renderer, inner, maxHeight, theme);
        export.measure(renderer, inner, maxHeight, theme);
        done.measure(renderer, inner, maxHeight, theme);
        float row1 = Math.max(rename.measuredHeight(), delete.measuredHeight());
        float row2 = Math.max(create.measuredHeight(), export.measuredHeight());
        measuredWidth = width;
        measuredHeight = Math.min(maxHeight, padding * 2 + renderer.lineHeight() + gap + name.measuredHeight()
                + gap + row1 + gap + row2 + gap * 2 + done.measuredHeight());
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
        float half = Math.max(0, (width - gap) / 2f);
        float row = Math.max(rename.measuredHeight(), delete.measuredHeight());
        rename.layout(renderer, new UiBounds(x, y, half, row), theme);
        delete.layout(renderer, new UiBounds(x + half + gap, y, half, row), theme);
        y += row + gap;
        row = Math.max(create.measuredHeight(), export.measuredHeight());
        create.layout(renderer, new UiBounds(x, y, half, row), theme);
        export.layout(renderer, new UiBounds(x + half + gap, y, half, row), theme);
        done.layout(renderer, new UiBounds(x + width - done.measuredWidth(),
                value.y() + value.height() - padding - done.measuredHeight(), done.measuredWidth(), done.measuredHeight()), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().surfaceRaised());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        float padding = theme.metrics().padding();
        renderer.drawText(UiText.translatable("screen.totemdoll.manage_style"), bounds.x() + padding, bounds.y() + padding, theme.palette().textPrimary());
        name.render(renderer, theme);
        rename.render(renderer, theme);
        delete.render(renderer, theme);
        create.render(renderer, theme);
        export.render(renderer, theme);
        if (status != null) Ui.drawFittedText(renderer, status, bounds.x() + padding,
                create.bounds().y() + create.bounds().height() + theme.metrics().spacing() / 2f,
                Math.max(0, bounds.width() - padding * 2), theme.palette().danger());
        done.render(renderer, theme);
    }

    private void rename() {
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
        if (style.templateId() == null) return;
        dialogs.show(new DollCreateDialog(dialogs, owner, DollStyles.get(style.templateId())));
    }

    private void exportStyle() {
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

    @Override public List<Ui.Node> childNodes() { return List.of(name, rename, delete, create, export, done); }
    @Override public boolean click(float x, float y, int button) {
        return done.click(x, y, button) || export.click(x, y, button) || create.click(x, y, button)
                || delete.click(x, y, button) || rename.click(x, y, button) || name.click(x, y, button);
    }
    @Override public boolean drag(float x, float y, int button) { return name.drag(x, y, button); }
    @Override public boolean release(float x, float y, int button) {
        return done.release(x, y, button) | export.release(x, y, button) | create.release(x, y, button)
                | delete.release(x, y, button) | rename.release(x, y, button) | name.release(x, y, button);
    }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) {
        return done.key(event, clipboard) || export.key(event, clipboard) || create.key(event, clipboard)
                || delete.key(event, clipboard) || rename.key(event, clipboard) || name.key(event, clipboard);
    }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return name.textInput(event, clipboard); }
}
