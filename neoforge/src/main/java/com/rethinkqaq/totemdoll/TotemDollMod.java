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
import com.rethinkqaq.totemdoll.doll.DollStyleLoader;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import com.rethinkqaq.totemdoll.doll.DollAnimationModels;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(Constants.MOD_ID)
public class TotemDollMod {

    private static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.totemdoll.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.totemdoll"
    );

    public TotemDollMod(IEventBus eventBus) {
        CommonClass.init();
        if (FMLEnvironment.dist.isClient()) {
            eventBus.addListener(this::registerAdditionalModels);
            eventBus.addListener(this::registerKeyMappings);
            NeoForge.EVENT_BUS.addListener(this::onClientTick);
            TotemDollClient.init(FMLPaths.CONFIGDIR.get(), FMLPaths.GAMEDIR.get());
        }
    }

    private void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        DollStyleLoader.reload(Minecraft.getInstance().getResourceManager())
                .forEach(style -> {
                    /*? if >=1.21.4 {*/
                    event.register(style.model());
                    /*?} else {*/
                    event.register(new ModelResourceLocation(style.model(), "standalone"));
                    /*?}*/
                    DollAnimationModels.modelIds(style).forEach(id -> {
                        /*? if >=1.21.4 {*/
                        event.register(id);
                        /*?} else {*/
                        event.register(new ModelResourceLocation(id, "standalone"));
                        /*?}*/
                    });
                });
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        DollAnimationManager.tick();
        TotemDollClient.reloadInitialStylesIfReady();
        while (OPEN_CONFIG.consumeClick()) {
            Minecraft client = Minecraft.getInstance();
            client.setScreen(new DollSelectionScreen(client.screen));
        }
    }
}
