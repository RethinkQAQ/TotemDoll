/*
 * Totem Doll
 * Copyright (C) 2026 Rethink_QAQ
 *
 * This file is part of Totem Doll.
 */

package com.rethinkqaq.totemdoll.client.gui.dialog;

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
import java.util.List;

/** Small confirmation surface used by destructive style operations. */
public final class DollConfirmDialog extends Ui.Node implements Ui.ChildProvider {
    private final UiDialogHost dialogs;
    private final UiText title;
    private final UiText message;
    private final UiButton confirm;
    private final UiButton keep;

    public DollConfirmDialog(UiDialogHost dialogs, UiText title, UiText message, Runnable confirmAction) {
        this.dialogs = dialogs;
        this.title = title;
        this.message = message;
        confirm = Ui.button(UiText.translatable("screen.totemdoll.delete"), () -> { dialogs.close(); confirmAction.run(); }).variant(Ui.ButtonVariant.DANGER);
        keep = Ui.button(UiText.translatable("screen.totemdoll.keep"), dialogs::close).variant(Ui.ButtonVariant.OUTLINE);
    }

    @Override protected void measureSelf(UiRenderer renderer, float maxWidth, float maxHeight, UiTheme theme) {
        float padding = theme.metrics().padding();
        float width = Math.min(maxWidth, 440);
        float gap = theme.metrics().spacing();
        confirm.measure(renderer, width, maxHeight, theme);
        keep.measure(renderer, width, maxHeight, theme);
        measuredWidth = width;
        measuredHeight = Math.min(maxHeight, padding * 2 + renderer.lineHeight() * 2 + gap * 3 + Math.max(confirm.measuredHeight(), keep.measuredHeight()));
    }

    @Override public void layout(UiRenderer renderer, UiBounds value, UiTheme theme) {
        super.layout(renderer, value, theme);
        float padding = theme.metrics().padding();
        float gap = theme.metrics().spacing();
        float width = Math.max(0, value.width() - padding * 2);
        float half = Math.max(0, (width - gap) / 2f);
        float y = value.y() + value.height() - padding - confirm.measuredHeight();
        keep.layout(renderer, new UiBounds(value.x() + padding, y, half, confirm.measuredHeight()), theme);
        confirm.layout(renderer, new UiBounds(value.x() + padding + half + gap, y, half, confirm.measuredHeight()), theme);
    }

    @Override public void render(UiRenderer renderer, UiTheme theme) {
        renderer.fillRoundRect(bounds, theme.metrics().cardRadius(), theme.palette().surfaceRaised());
        renderer.strokeRoundRect(bounds, theme.metrics().cardRadius(), theme.metrics().borderWidth(), theme.palette().border());
        float padding = theme.metrics().padding();
        renderer.drawText(title, bounds.x() + padding, bounds.y() + padding, theme.palette().textPrimary());
        Ui.drawFittedText(renderer, message, bounds.x() + padding, bounds.y() + padding + renderer.lineHeight() + theme.metrics().spacing() / 2f,
                Math.max(0, bounds.width() - padding * 2), theme.palette().textSecondary());
        keep.render(renderer, theme);
        confirm.render(renderer, theme);
    }

    @Override public List<Ui.Node> childNodes() { return List.of(keep, confirm); }
    @Override public boolean click(float x, float y, int button) { return confirm.click(x, y, button) || keep.click(x, y, button); }
    @Override public boolean release(float x, float y, int button) { return confirm.release(x, y, button) | keep.release(x, y, button); }
    @Override public boolean key(UiKeyEvent event, UiClipboard clipboard) { return confirm.key(event, clipboard) || keep.key(event, clipboard); }
    @Override public boolean textInput(UiTextInput event, UiClipboard clipboard) { return false; }
}
