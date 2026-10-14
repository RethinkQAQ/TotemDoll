import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceTask

plugins {
    java
    idea
    `java-library`
    id("license-conventions")
}

// Stonecutter creates version-specific Java sources during the build. Ensure
// those generated sources receive the license header before compilation.
listOf("licenseFormatMain", "licenseMain").forEach { taskName ->
    tasks.named<SourceTask>(taskName) {
        dependsOn(stonecutterGenerateTask)
        source(stonecutterGeneratedMain.map { it.dir("java") })
    }
}

val buildSuffix = commonMod.propOrNull("build.number")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }

version = listOfNotNull("${commonMod.version}-mc${commonMod.mc}", buildSuffix)
    .joinToString("-")

base { archivesName = "${commonMod.id}-${loader}" }
group = "${commonMod.group}.${loader ?: "common"}"

val configuredJavaVersion = commonMod.prop("java.version").toInt()
val mixinCompatibilityLevel = "JAVA_$configuredJavaVersion"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(configuredJavaVersion)
    sourceCompatibility = JavaVersion.toVersion(configuredJavaVersion)
    targetCompatibility = JavaVersion.toVersion(configuredJavaVersion)
}

val runJavaLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(configuredJavaVersion))
}
tasks.withType<JavaExec>().configureEach { javaLauncher.set(runJavaLauncher) }
gradle.projectsEvaluated {
    tasks.withType<JavaExec>().matching { it.name in setOf("runClient", "runServer") }.configureEach {
        setExecutable(runJavaLauncher.get().executablePath.asFile.absolutePath)
    }
}
tasks.register("printRunJava") {
    group = "help"
    doLast { println("Minecraft ${commonMod.mc}: ${runJavaLauncher.get().executablePath.asFile}") }
}

repositories {
    mavenCentral()
    if (loader != "neoforge") maven("https://maven.fabricmc.net/") {
        name = "FabricMC"
        content {
            includeGroupByRegex("(?:remapped\\.)?net\\.fabricmc(\\..*)?")
        }
    }
    if (loader == "neoforge") maven("https://maven.neoforged.net/releases/") {
        name = "NeoForged"
        content {
            includeGroupByRegex("(?:remapped\\.)?net\\.neoforged(\\..*)?")
            includeGroup("codechicken")
        }
    }
    strictMaven("https://repo.spongepowered.org/repository/maven-public", "Sponge", "org.spongepowered")
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    if (loader != "neoforge") maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
        content { includeGroup("com.terraformersmc") }
    }
    if (loader != "neoforge") maven("https://maven.nucleoid.xyz") { name = "Nucleoid" }
    maven("https://jitpack.io/") {
        name = "JitPack"
        content { includeGroupByRegex("com\\.github\\..+") }
    }
}

tasks {
    processResources {
        val packFormat = commonMod.prop("resource_pack_format").split('.').map(String::toInt)
        val resourcePackMetadata = if (commonMod.minecraftVersion.atLeast(1, 21, 9)) {
            val format = "[${packFormat[0]}, ${packFormat.getOrElse(1) { 0 }}]"
            "\"min_format\": $format,\n        \"max_format\": $format"
        } else {
            "\"pack_format\": ${packFormat[0]}"
        }
        val expandProps = mapOf(
        "resourcePackMetadata" to resourcePackMetadata,
        "mixinCompatibilityLevel" to mixinCompatibilityLevel,
        "modId" to commonMod.id,
        "modName" to commonMod.name,
        "modVersion" to commonMod.version,
        "modGroup" to commonMod.group,
        "modAuthor" to commonMod.author,
        "modDescription" to commonMod.description,
        "modLicense" to commonMod.license,
        "modGitHub" to commonMod.github,
        "minecraftVersion" to commonMod.prop("minecraft_version"),
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
        filesMatching(listOf("fabric.mod.json", "*.mixins.json")) {
            expand(jsonExpandProps)
        }
        filesMatching("pack.mcmeta") { expand(expandProps) }
        inputs.properties(expandProps)
    }
}

tasks.named("processResources") {
    dependsOn(stonecutterGenerateTask)
}
