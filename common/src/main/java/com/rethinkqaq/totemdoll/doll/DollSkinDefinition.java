package com.rethinkqaq.totemdoll.doll;

public record DollSkinDefinition(
        String format,
        String textureSlot
) {

    public static final String MINECRAFT_64X64 = "minecraft_64x64";

    public boolean supportsImport() {
        return MINECRAFT_64X64.equals(format)
                && textureSlot != null
                && !textureSlot.isBlank();
    }
}
