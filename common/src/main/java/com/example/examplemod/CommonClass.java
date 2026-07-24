package com.example.examplemod;

public final class CommonClass {

    public static void init() {
        Constants.LOG.info("Initializing {}", Constants.MOD_NAME);
    }

    private CommonClass() {
    }
}
