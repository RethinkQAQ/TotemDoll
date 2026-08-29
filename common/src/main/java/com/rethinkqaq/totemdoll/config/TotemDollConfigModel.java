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

import com.rethinkqaq.configui.config.annotation.RcuiConfig;
import com.rethinkqaq.configui.config.annotation.Setting;
import com.rethinkqaq.totemdoll.utils.DollResourceId;

@RcuiConfig(id = "totemdoll", file = "totemdoll.yaml", wrapperName = "TotemDollConfig", schemaVersion = 1)
public final class TotemDollConfigModel {
    @Setting(section = "appearance", title = "Selected style", codec = DollResourceIdCodec.class)
    public DollResourceId selectedStyle = new DollResourceId("totemdoll:alex");

    @Setting(section = "skin_layer_3d", key = "enabled", title = "Enable 3D skin layer")
    public boolean skinLayer3dEnabled = false;

    @Setting(section = "skin_layer_3d", key = "thickness", title = "Skin layer thickness", min = 0.05, max = 1.0, step = 0.05)
    public float skinLayer3dThickness = 0.5F;

    @Setting(section = "skin_layer_3d", key = "distance", title = "Skin layer distance", min = 0.0, max = 64.0, step = 1.0)
    public float skinLayer3dDistance = 12.0F;

    public TotemDollConfigModel() {
    }
}
