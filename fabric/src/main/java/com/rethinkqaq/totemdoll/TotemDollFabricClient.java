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

package com.rethinkqaq.totemdoll;

import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.client.gui.DollSelectionScreen;
import com.rethinkqaq.totemdoll.client.gui.screen.DollScreenAdapter;
import com.rethinkqaq.totemdoll.doll.DollStyleLoader;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? >= 26.1.2 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?}
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

//? >= 1.21.10 {
/*import com.rethinkqaq.totemdoll.utils.DollMinecraftResourceUtil;
import com.rethinkqaq.totemdoll.utils.DollResourceId;
*///?}

public final class TotemDollFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TotemDollClient.init(
                FabricLoader.getInstance().getConfigDir(),
                FabricLoader.getInstance().getGameDir()
        );
        ModelLoadingPlugin.register(context ->
                DollStyleLoader.reload(Minecraft.getInstance().getResourceManager()));
        KeyMapping openConfig =
                //? >= 26.1.2 {
                /*KeyMappingHelper.registerKeyMapping(
                *///?} else {
                KeyBindingHelper.registerKeyBinding(
                //?}
                        new KeyMapping(
                                "key.totemdoll.open_config",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_F9,
                                //? >= 1.21.10 {
                                /*KeyMapping.Category.register(DollMinecraftResourceUtil.nativeId(
                                        DollResourceId.ofVanilla("totemdoll")))
                                *///?} else {
                                "key.categories.totemdoll"
                                 //?}
                        )
                );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DollAnimationManager.tick();
            TotemDollClient.reloadInitialStylesIfReady();
            while (openConfig.consumeClick()) {
                DollScreenAdapter.setScreen(client, new DollSelectionScreen(DollScreenAdapter.currentScreen(client)));
            }
        });
    }
}
