plugins {
    id("multiloader-loader")
    id("fabric-loom-compat")
    id("com.github.hierynomus.license") version "0.16.1"
}

apply(from = rootProject.file("license.gradle"))

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (stonecutter.eval(commonMod.mc, "<=1.21.11")) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:${commonMod.dep("parchment")}@zip")
        })
    }
    modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric-loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric-api")}+${commonMod.mc}")
    modImplementation("com.terraformersmc:modmenu:${commonMod.dep("modmenu")}")

    modImplementation("com.github.RethinkQAQ.RethinkConfigUiLib:rethink-config-ui-lib-mc${commonMod.mc}:0.1.2-local")
    include("com.github.RethinkQAQ.RethinkConfigUiLib:rethink-config-ui-lib-mc${commonMod.mc}:0.1.2-local")
}

configureTotemDollMixinSupport(TotemDollMixinTarget.FABRIC)

if (!commonMod.unobfuscated) {
    loom {
        mixin {
            useLegacyMixinAp.set(true)
            defaultRefmapName.set("${commonMod.id}.refmap.json")
        }
    }
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric Client ${commonMod.mc}"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") { isIdeConfigGenerated = false }
    }
}

tasks.matching { it.name == "runServer" }.configureEach { enabled = false }
