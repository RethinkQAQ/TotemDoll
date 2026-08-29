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

package com.rethinkqaq.totemdoll.config;

import com.rethinkqaq.configui.config.ConfigCodec;
import com.rethinkqaq.configui.config.ConfigCodecException;
import com.rethinkqaq.totemdoll.utils.DollResourceId;

public final class DollResourceIdCodec implements ConfigCodec<DollResourceId> {
    public DollResourceIdCodec() {
    }

    @Override public Object encode(DollResourceId value) {
        return value.toString();
    }

    @Override public DollResourceId decode(Object value) throws ConfigCodecException {
        if (!(value instanceof String text)) throw new ConfigCodecException("Expected a resource identifier string");
        DollResourceId parsed = DollResourceId.tryParse(text);
        if (parsed == null) throw new ConfigCodecException("Invalid resource identifier: " + text);
        return parsed;
    }
}
