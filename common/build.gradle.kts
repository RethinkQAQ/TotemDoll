plugins {
    id("multiloader-common")
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
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")
}

loom {
    mixin {
        defaultRefmapName.set("${requiredProperty("mod_id")}.refmap.json")
    }
}

val commonJava by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}
val commonResources by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    afterEvaluate {
        sourceSets.main.get().java.sourceDirectories.files.forEach {
            add(commonJava.name, it)
        }
        sourceSets.main.get().resources.sourceDirectories.files.forEach {
            add(commonResources.name, it)
        }
    }
}
