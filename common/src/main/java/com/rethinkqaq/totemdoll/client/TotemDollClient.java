package com.rethinkqaq.totemdoll.client;

import com.rethinkqaq.totemdoll.config.TotemDollConfig;
import com.rethinkqaq.totemdoll.doll.DollLocalStyleStore;
import com.rethinkqaq.totemdoll.doll.DollStyles;
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
        // The generated pack is enabled before the normal resource load, but
        // the resource manager may not have included it yet. Let the first
        // client tick perform one reload after the manager is ready so saved
        // local styles are visible on the first launch.
        initialStylesReloaded = false;
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
