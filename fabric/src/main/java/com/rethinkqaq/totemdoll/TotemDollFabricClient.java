package com.rethinkqaq.totemdoll;

import com.rethinkqaq.totemdoll.client.TotemDollClient;
import com.rethinkqaq.totemdoll.client.gui.DollSelectionScreen;
import com.rethinkqaq.totemdoll.doll.DollStyleLoader;
import com.rethinkqaq.totemdoll.doll.DollAnimationManager;
import com.rethinkqaq.totemdoll.doll.DollAnimationModels;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class TotemDollFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TotemDollClient.init(
                FabricLoader.getInstance().getConfigDir(),
                FabricLoader.getInstance().getGameDir()
        );
        ModelLoadingPlugin.register(context -> context.addModels(
                DollStyleLoader.reload(Minecraft.getInstance().getResourceManager()).stream()
                        .flatMap(style -> java.util.stream.Stream.concat(
                                java.util.stream.Stream.of(style.model()),
                                DollAnimationModels.modelIds(style).stream()))
                        .toList()
        ));
        KeyMapping openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.totemdoll.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "key.categories.totemdoll"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            DollAnimationManager.tick();
            TotemDollClient.reloadInitialStylesIfReady();
            while (openConfig.consumeClick()) {
                client.setScreen(new DollSelectionScreen(client.screen));
            }
        });
    }
}
