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

package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.doll.DollStyle;

public final class DollPreviewContext {

    private static final ThreadLocal<DollStyle> PREVIEW_STYLE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> NATIVE_RENDER = ThreadLocal.withInitial(() -> false);

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

    public static boolean isNativeRender() {
        return NATIVE_RENDER.get();
    }

    public static void renderNative(Runnable renderCall) {
        boolean previous = NATIVE_RENDER.get();
        NATIVE_RENDER.set(true);
        try {
            renderCall.run();
        } finally {
            if (previous) {
                NATIVE_RENDER.set(true);
            } else {
                NATIVE_RENDER.remove();
            }
        }
    }

    private DollPreviewContext() {
    }
}
