plugins {
    id("multiloader-common")
    id("fabric-loom") version "1.17-SNAPSHOT"
    id("com.github.hierynomus.license") version "0.16.1"
}

apply(from = rootProject.file("license.gradle"))

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${commonMod.mc}:${commonMod.dep("parchment")}@zip")
    })
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")
}

loom {
    mixin { defaultRefmapName.set("${commonMod.id}.refmap.json") }
}

val commonJava: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}
val commonResources: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    afterEvaluate {
        val main = sourceSets.main.get()
        main.java.sourceDirectories.files.forEach { add(commonJava.name, it) }
        main.resources.sourceDirectories.files.forEach { add(commonResources.name, it) }
    }
}
