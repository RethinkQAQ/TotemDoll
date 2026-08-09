plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.143" apply false
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    base
}

apply(from = rootProject.file("build.gradle.kts"))

stonecutter.active("26.2")
