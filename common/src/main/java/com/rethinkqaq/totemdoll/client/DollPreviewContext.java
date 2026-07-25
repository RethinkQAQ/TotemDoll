package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.doll.DollStyle;

public final class DollPreviewContext {

    private static final ThreadLocal<DollStyle> PREVIEW_STYLE = new ThreadLocal<>();

    public static DollStyle current() {
        return PREVIEW_STYLE.get();
    }

    public static void renderAs(DollStyle style, Runnable renderCall) {
        DollStyle previous = PREVIEW_STYLE.get();
        PREVIEW_STYLE.set(style);
        try {
            renderCall.run();
        } finally {
            if (previous == null) {
                PREVIEW_STYLE.remove();
            } else {
                PREVIEW_STYLE.set(previous);
            }
        }
    }

    private DollPreviewContext() {
    }
}
