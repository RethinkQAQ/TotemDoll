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

package com.rethinkqaq.totemdoll.utils;

import java.nio.file.Path;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class DollFileDialog {
    private DollFileDialog() {
    }

    public static Path open(String title, String description, String extension) {
        String selected = TinyFileDialogs.tinyfd_openFileDialog(title, "", null, description, false);
        return selected == null || selected.isBlank() ? null : Path.of(selected);
    }

    public static Path save(String title, String defaultName, String description, String extension) {
        String selected = TinyFileDialogs.tinyfd_saveFileDialog(title, defaultName, null, description);
        return selected == null || selected.isBlank() ? null : Path.of(selected);
    }
}
