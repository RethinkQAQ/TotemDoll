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
                    event.register(ModelResourceLocation.standalone(style.model()));
                    DollAnimationModels.modelIds(style).forEach(id ->
                            event.register(ModelResourceLocation.standalone(id)));
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
