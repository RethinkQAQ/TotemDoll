plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.143"
    id("com.github.hierynomus.license") version "0.16.1"
}

apply(from = rootProject.file("license.gradle"))

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
        }
    }
    mods {
        register(commonMod.id) {
            sourceSet(sourceSets.main.get())
        }
    }
}
