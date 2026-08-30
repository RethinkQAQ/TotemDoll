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

package com.rethinkqaq.totemdoll.client.gui.preview;

//? >= 1.21.6 {
/*import com.rethinkqaq.totemdoll.client.gui.preview.DollGuiPreviewRenderState.PreviewKey;

import java.util.HashMap;
import java.util.Map;

final class DollGuiPreviewTargetCache implements AutoCloseable {
    private final Map<PreviewKey, DollGuiPreviewTarget> targets = new HashMap<>();

    public DollGuiPreviewTarget getOrCreate(PreviewKey key) {
        return targets.computeIfAbsent(key,
                ignored -> new DollGuiPreviewTarget(key.width(), key.height()));
    }

    public void clear() {
        targets.values().forEach(DollGuiPreviewTarget::close);
        targets.clear();
    }

    @Override
    public void close() {
        clear();
    }
}
*///?} else {
final class DollGuiPreviewTargetCache {
    public void clear() {
    }
}
//?}
