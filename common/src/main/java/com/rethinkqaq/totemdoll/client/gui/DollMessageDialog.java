/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 */

package com.rethinkqaq.totemdoll.client.gui;

import com.rethinkqaq.configui.core.Ui;
import com.rethinkqaq.configui.core.UiBounds;
import com.rethinkqaq.configui.core.UiClipboard;
import com.rethinkqaq.configui.core.UiDialogHost;
import com.rethinkqaq.configui.core.UiKeyEvent;
import com.rethinkqaq.configui.core.UiRenderer;
import com.rethinkqaq.configui.core.UiText;
import com.rethinkqaq.configui.core.UiTextInput;
import com.rethinkqaq.configui.core.UiTheme;
import com.rethinkqaq.configui.core.component.UiButton;
import com.rethinkqaq.configui.core.component.feedback.UiAlert;
import com.rethinkqaq.configui.core.component.feedback.UiFeedbackType;

import java.util.List;

/** Informational dialog for operations that fail before a page refresh. */
final class DollMessageDialog extends Ui.Node implements Ui.ChildProvider {
    private final UiDialogHost dialogs;
    private final UiText title;
    private final UiAlert alert;
    private final UiButton done;

    DollMessageDialog(UiDialogHost dialogs, UiText title, UiText message) {
        this.dialogs = dialogs;
        this.title = title;
        this.alert = Ui.alert(UiFeedbackType.ERROR, message);
        this.done = Ui.button(UiText.translatable("gui.done"), dialogs::close).variant(Ui.ButtonVariant.PRIMARY);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding();
        float width = Math.min(maxWidth, 520);
        float inner = Math.max(0, width - padding * 2);
        float gap = theme.metrics().spacing();
        alert.measure(renderer, inner, maxHeight, theme);
        done.measure(renderer, inner, maxHeight, theme);
        measuredWidth = width;
        measuredHeight = Math.min(maxHeight, padding * 2 + renderer.lineHeight() + gap
                + alert.measuredHeight() + gap + done.measuredHeight());
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float padding = theme.metrics().padding();
        float gap = theme.metrics().spacing();
        float x = value.x() + padding;
        float width = Math.max(0, value.width() - padding * 2);
        float y = value.y() + padding + renderer.lineHeight() + gap;
        alert.layout(renderer, new UiBounds(x, y, width, alert.measuredHeight()), theme);
        done.layout(renderer, new UiBounds(value.x() + value.width() - padding - done.measuredWidth(),
                value.y() + value.height() - padding - done.measuredHeight(),
                done.measuredWidth(), done.measuredHeight()), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().surfaceRaised());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        float padding = theme.metrics().padding();
        renderer.drawText(title, bounds.x() + padding, bounds.y() + padding, theme.palette().textPrimary());
        alert.render(renderer, theme);
        done.render(renderer, theme);
    }

    @Override public List<Ui.Node> childNodes() { return List.of(alert, done); }
    @Override public boolean click(float x, float y, int button) { return done.click(x, y, button) || alert.click(x, y, button); }
    @Override public boolean release(float x, float y, int button) { return done.release(x, y, button) || alert.release(x, y, button); }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return done.key(event, clipboard); }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return false; }
}
