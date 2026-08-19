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

package com.rethinkqaq.totemdoll.client.gui;

//? >= 1.21.6 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
//? >= 26.2 {
/^import com.mojang.blaze3d.GpuFormat;
^///?} else {
import com.mojang.blaze3d.textures.TextureFormat;
//?}

final class DollGuiPreviewTarget implements AutoCloseable {
    final GpuTexture color;
    final GpuTextureView colorView;
    final GpuTexture depth;
    final GpuTextureView depthView;
    private boolean rendered;
    private boolean closed;

    DollGuiPreviewTarget(int width, int height) {
        var device = RenderSystem.getDevice();
        //? >= 26.2 {
        /^color = device.createTexture(() -> "TotemDoll GUI preview", 13,
                GpuFormat.RGBA8_UNORM, width, height, 1, 1);
        colorView = device.createTextureView(color);
        depth = device.createTexture(() -> "TotemDoll GUI preview depth", 9,
                GpuFormat.D32_FLOAT, width, height, 1, 1);
        ^///?} else {
        //? >= 26.1.2 {
        /^color = device.createTexture(() -> "TotemDoll GUI preview", 13,
                TextureFormat.RGBA8, width, height, 1, 1);
        colorView = device.createTextureView(color);
        depth = device.createTexture(() -> "TotemDoll GUI preview depth", 9,
                TextureFormat.DEPTH32, width, height, 1, 1);
        ^///?} else {
        color = device.createTexture(() -> "TotemDoll GUI preview", 12,
                TextureFormat.RGBA8, width, height, 1, 1);
        colorView = device.createTextureView(color);
        depth = device.createTexture(() -> "TotemDoll GUI preview depth", 8,
                TextureFormat.DEPTH32, width, height, 1, 1);
        //?}
        //?}
        depthView = device.createTextureView(depth);
    }

    boolean needsRender(boolean dynamic) {
        return dynamic || !rendered;
    }

    void markRendered() {
        rendered = true;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        colorView.close();
        color.close();
        depthView.close();
        depth.close();
    }
}
*///?} else {
final class DollGuiPreviewTarget {
}
//?}
