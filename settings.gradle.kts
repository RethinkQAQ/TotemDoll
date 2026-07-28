pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://repo.spongepowered.org/repository/maven-public")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val commonVersions = listOf(
    "1.21.1",
    "1.21.4"
)
val fabricVersions = commonVersions
val neoForgeVersions = commonVersions

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions(*commonVersions.toTypedArray())
        branch("common") {
            versions(*commonVersions.toTypedArray())
        }
        branch("fabric") {
            versions(*fabricVersions.toTypedArray())
        }
        branch("neoforge") {
            versions(*neoForgeVersions.toTypedArray())
        }
    }
}

rootProject.name = "TotemDoll"
