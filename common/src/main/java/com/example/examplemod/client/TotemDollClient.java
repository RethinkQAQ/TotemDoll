package com.example.examplemod.client;

import com.example.examplemod.config.TotemDollConfig;
import com.example.examplemod.doll.DollStyles;

import java.nio.file.Path;

public final class TotemDollClient {

    public static void init(Path configDirectory) {
        DollStyles.init();
        TotemDollConfig.initialize(configDirectory);
    }

    private TotemDollClient() {
    }
}
