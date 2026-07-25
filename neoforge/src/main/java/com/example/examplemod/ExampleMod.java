package com.example.examplemod;

import com.example.examplemod.client.TotemDollClient;
import com.example.examplemod.client.gui.DollSelectionScreen;
import com.example.examplemod.doll.DollStyleLoader;
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
public class ExampleMod {

    private static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.totemdoll.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.totemdoll"
    );

    public ExampleMod(IEventBus eventBus) {
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
                .forEach(style -> event.register(ModelResourceLocation.standalone(style.model())));
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        TotemDollClient.reloadInitialStylesIfReady();
        while (OPEN_CONFIG.consumeClick()) {
            Minecraft client = Minecraft.getInstance();
            client.setScreen(new DollSelectionScreen(client.screen));
        }
    }
}
