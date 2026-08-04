plugins {
    java
    idea
    `java-library`
}

val buildSuffix = commonMod.propOrNull("build.number")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }

version = listOfNotNull("${commonMod.version}-${loader}", buildSuffix)
    .joinToString("-")

base { archivesName = commonMod.id }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(commonProject.prop("java.version")!!)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") {
        name = "FabricMC"
        content {
            includeGroupByRegex("net\\.fabricmc(\\..*)?")
        }
    }
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForged"
        content {
            includeGroupByRegex("net\\.neoforged(\\..*)?")
            includeGroup("codechicken")
        }
    }
    strictMaven("https://repo.spongepowered.org/repository/maven-public", "Sponge", "org.spongepowered")
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
        content { includeGroup("com.terraformersmc") }
    }
}

tasks {
    processResources {
        val expandProps = mapOf(
        "modId" to commonMod.id,
        "modName" to commonMod.name,
        "modVersion" to commonMod.version,
        "modGroup" to commonMod.group,
        "modAuthor" to commonMod.author,
        "modDescription" to commonMod.description,
        "modLicense" to commonMod.license,
        "modGitHub" to commonMod.github,
        "minecraftVersion" to commonMod.prop("minecraft_version"),
        "minMinecraftVersion" to commonMod.prop("min_minecraft_version"),
        "fabricLoaderVersion" to commonMod.dep("fabric-loader"),
        "fabricLoaderMinVersion" to commonMod.dep("fabric-loader-min"),
        "fabricApiVersion" to commonMod.dep("fabric-api"),
        "neoForgeVersion" to commonMod.dep("neoforge"),
        "neoForgeMinVersion" to commonMod.dep("neoforge-min"),
        "javaVersion" to commonMod.prop("java.version"),
        // Standard placeholder names used by the Stonecutter multiloader template.
        "mod_id" to commonMod.id,
        "version" to commonMod.version,
        "mod_name" to commonMod.name,
        "description" to commonMod.description,
        "mod_author" to commonMod.author,
        "license" to commonMod.license,
        "minecraft_version" to commonMod.prop("minecraft_version"),
        "minecraft_version_range_fabric" to commonMod.prop("minecraft_version_range_fabric"),
        "java_version" to commonMod.prop("java.version"),
        "fabric_loader_version" to commonMod.dep("fabric-loader"),
        "fabric_loader_min_version" to commonMod.dep("fabric-loader-min"),
        "fabric_api_version" to commonMod.dep("fabric-api"),
        "neoforge_version" to commonMod.dep("neoforge"),
        "neoforge_min_version" to commonMod.dep("neoforge-min"),
        "minecraftVersionRangeNeoForge" to commonMod.prop("minecraft_version_range_neoforge")
        ).filterValues { !it.isNullOrEmpty() }.mapValues { (_, value) -> value }

        val jsonExpandProps = expandProps.mapValues { (_, value) ->
            value.replace("\n", "\\\\n")
        }

        filesMatching(listOf("META-INF/neoforge.mods.toml")) {
            expand(expandProps)
        }
        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
            expand(jsonExpandProps)
        }
        inputs.properties(expandProps)
    }
}

tasks.named("processResources") {
    dependsOn(":common:${commonMod.prop("minecraft_version")}:stonecutterGenerate")
}
