plugins {
    java
    idea
    `java-library`
}

version = "${loader}-${commonMod.version}+mc${stonecutterBuild.current.version}"

base { archivesName = commonMod.id }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(commonProject.prop("java.version")!!)
}

repositories {
    mavenCentral()
    strictMaven("https://repo.spongepowered.org/repository/maven-public", "Sponge", "org.spongepowered")
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

tasks.processResources {
    val values = mapOf(
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
        "fabricApiVersion" to commonMod.dep("fabric-api"),
        "neoForgeVersion" to commonMod.dep("neoforge"),
        "javaVersion" to commonMod.prop("java.version")
    )
    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(values)
    }
    inputs.properties(values)
}
