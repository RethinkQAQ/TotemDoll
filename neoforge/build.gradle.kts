plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.147"
}

configureTotemDollMixinSupport(TotemDollMixinTarget.NEOFORGE)

neoForge {
    version = commonMod.dep("neoforge")
    if (stonecutter.eval(commonMod.mc, "<=1.21.11")) {
        parchment {
            minecraftVersion = commonMod.mc
            mappingsVersion = commonMod.dep("parchment")
        }
    }
    runs {
        register("client") {
            client()
            ideName = "NeoForge Client ${commonMod.mc}"
            disableIdeRun()
        }
    }
    mods {
        register(commonMod.id) {
            sourceSet(sourceSets.main.get())
        }
    }
    dependencies {
        val rcui = "com.github.RethinkQAQ.RethinkConfigUiLib:rethink-config-ui-lib-${commonMod.mc}-neoforge:${commonMod.prop("mod.rcui")}"

        jarJar(implementation(rcui) { isTransitive = false })

        if (stonecutter.eval(commonMod.mc, ">=26.3")) {
            val tinyfd = "org.lwjgl:lwjgl-tinyfd:${commonMod.dep("lwjgl-tinyfd")}"
            jarJar(implementation(tinyfd)!!)
            listOf("windows", "windows-arm64", "linux", "linux-arm64", "macos", "macos-arm64").forEach { platform ->
                jarJar(implementation("$tinyfd:natives-$platform")!!)
            }
        }
    }
}

tasks.matching { it.name == "runClient" }.configureEach {
    enabled = providers.gradleProperty("run.client").map(String::toBoolean).orElse(true).get()
}
