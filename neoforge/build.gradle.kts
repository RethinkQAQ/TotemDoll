plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev") version "2.0.49-beta"
    id("com.github.hierynomus.license") version "0.16.1"
}

apply(from = rootProject.file("license.gradle"))

neoForge {
    version = requiredProperty("neoforge_version")
    val accessTransformer = rootProject.file("common/src/main/resources/META-INF/accesstransformer.cfg")
    if (accessTransformer.exists()) {
        accessTransformers.from(accessTransformer.absolutePath)
    }
    parchment {
        minecraftVersion = requiredProperty("parchment_minecraft")
        mappingsVersion = requiredProperty("parchment_version")
    }
    runs {
        register("client") {
            client()
            ideName = "NeoForge Client $minecraftVersion"
        }
    }
    mods {
        register(requiredProperty("mod_id")) {
            sourceSet(sourceSets.main.get())
        }
    }
}
