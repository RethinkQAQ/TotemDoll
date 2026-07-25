package com.example.examplemod.doll;

import com.example.examplemod.Constants;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class DollStyles {

    public static final ResourceLocation VANILLA_ID =
            ResourceLocation.withDefaultNamespace("default");
    public static final ResourceLocation ALEX_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "alex");

    public static final DollStyle VANILLA = new DollStyle(
            VANILLA_ID,
            "Vanilla Totem",
            "doll.totemdoll.vanilla",
            ResourceLocation.withDefaultNamespace("totem_of_undying"),
            false,
            null,
            false,
            null,
            DollStyleOrigin.BUILTIN,
            Map.of(), List.of(), "minecraft_item", null
    );

    private static final Map<ResourceLocation, DollStyle> STYLES = new LinkedHashMap<>();

    static {
        register(VANILLA);
    }

    public static void init() {
        Constants.LOG.info("Loaded {} Totem Doll styles", STYLES.size());
    }

    public static synchronized DollStyle get(ResourceLocation id) {
        return STYLES.getOrDefault(id, STYLES.getOrDefault(ALEX_ID, VANILLA));
    }

    public static synchronized List<DollStyle> all() {
        return List.copyOf(STYLES.values());
    }

    public static synchronized void replaceDiscovered(Collection<DollStyle> styles) {
        STYLES.clear();
        register(VANILLA);
        styles.forEach(DollStyles::register);
        Constants.LOG.info("Loaded {} Totem Doll styles", STYLES.size());
    }

    private static void register(DollStyle style) {
        if (STYLES.putIfAbsent(style.id(), style) != null) {
            throw new IllegalStateException("Duplicate doll style " + style.id());
        }
    }

    private DollStyles() {
    }
}
