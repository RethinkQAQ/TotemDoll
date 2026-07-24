package com.example.examplemod;

import com.example.examplemod.client.TotemDollClient;
import com.example.examplemod.client.gui.DollSelectionScreen;
import com.example.examplemod.doll.DollStyleLoader;
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
        ModelLoadingPlugin.register(context -> context.addModels(
                DollStyleLoader.reload(Minecraft.getInstance().getResourceManager())
                        .stream()
                        .map(style -> style.model())
                        .toList()
        ));
        TotemDollClient.init(FabricLoader.getInstance().getConfigDir());

        KeyMapping openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.totemdoll.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "key.categories.totemdoll"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfig.consumeClick()) {
                client.setScreen(new DollSelectionScreen(client.screen));
            }
        });
    }
}
