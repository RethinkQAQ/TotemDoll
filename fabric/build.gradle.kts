plugins {
    id("multiloader-loader")
    id("fabric-loom-compat")
}

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (stonecutter.eval(commonMod.mc, "<=1.21.11")) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:${commonMod.dep("parchment")}@zip")
        })
    }
    modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric-loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric-api")}+${commonMod.mc}") {
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }
    modImplementation("com.terraformersmc:modmenu:${commonMod.dep("modmenu")}") {
        exclude(group = "net.fabricmc", module = "fabric-loader")
    }

    if (stonecutter.eval(commonMod.mc, ">=26.3")) {
        val tinyfd = "org.lwjgl:lwjgl-tinyfd:${commonMod.dep("lwjgl-tinyfd")}"
        include(implementation(tinyfd)!!)
        listOf("windows", "windows-arm64", "linux", "linux-arm64", "macos", "macos-arm64").forEach { platform ->
            include(runtimeOnly("$tinyfd:natives-$platform")!!)
        }
    }

    val rcui = "com.github.RethinkQAQ.RethinkConfigUiLib:rethink-config-ui-lib-${commonMod.mc}-fabric:${commonMod.prop("mod.rcui")}"

    include(modImplementation(rcui) { isTransitive = false })
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
            ideConfigGenerated(false)
            runDir("runs/client")
        }
        named("server") { isIdeConfigGenerated = false }
    }
}

tasks.matching { it.name == "runClient" }.configureEach {
    enabled = providers.gradleProperty("run.client").map(String::toBoolean).orElse(true).get()
}
tasks.matching { it.name == "runServer" }.configureEach {
    enabled = providers.gradleProperty("run.server").map(String::toBoolean).orElse(false).get()
}
