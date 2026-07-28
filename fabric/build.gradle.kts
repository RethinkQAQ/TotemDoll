plugins {
    id("multiloader-loader")
    id("fabric-loom") version "1.8.13"
    id("com.github.hierynomus.license") version "0.16.1"
}

apply(from = rootProject.file("license.gradle"))

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${requiredProperty("parchment_minecraft")}:${requiredProperty("parchment_version")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${requiredProperty("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${requiredProperty("fabric_version")}")
}

loom {
    val accessWidener = rootProject.file("common/src/main/resources/${requiredProperty("mod_id")}.accesswidener")
    if (accessWidener.exists()) {
        accessWidenerPath.set(accessWidener)
    }
    mixin {
        defaultRefmapName.set("${requiredProperty("mod_id")}.refmap.json")
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client $minecraftVersion"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            isIdeConfigGenerated = false
        }
    }
}

tasks.matching { it.name == "runServer" }.configureEach {
    enabled = false
    group = null
    description = "Disabled: Totem Doll is a client-only mod."
}
