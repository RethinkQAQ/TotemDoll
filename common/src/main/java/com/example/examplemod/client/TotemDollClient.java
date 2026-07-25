package com.example.examplemod.client;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.doll.DollLocalStyleStore;
import com.example.examplemod.doll.DollStyles;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class TotemDollClient {

    private static boolean initialStylesReloaded;

    public static void init(Path configDirectory, Path gameDirectory) {
        DollStyles.init();
        TotemDollConfig.initialize(configDirectory);
        DollLocalStyleStore.initialize(configDirectory, gameDirectory);
        Minecraft client = Minecraft.getInstance();
        enableGeneratedPack(client);
        // The pack is enabled before Minecraft performs its normal initial
        // resource load. Do not trigger a second full reload from the first
        // client tick.
        initialStylesReloaded = true;
    }

    /** Reloads the generated pack once the client resource manager is ready. */
    public static boolean reloadInitialStylesIfReady() {
        if (initialStylesReloaded) {
            return true;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getResourceManager() == null) {
            return false;
        }
        initialStylesReloaded = true;
        reloadGeneratedStyles();
        return true;
    }

    public static CompletableFuture<Void> reloadGeneratedStyles() {
        Minecraft client = Minecraft.getInstance();
        enableGeneratedPack(client);
        return client.reloadResourcePacks();
    }

    private static void enableGeneratedPack(Minecraft client) {
        PackRepository repository = client.getResourcePackRepository();
        repository.reload();
        if (!repository.addPack(DollLocalStyleStore.GENERATED_PACK_ID)) {
            repository.setSelected(repository.getSelectedIds());
        }
    }

    private TotemDollClient() {
    }
}
