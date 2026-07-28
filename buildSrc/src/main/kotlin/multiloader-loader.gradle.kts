plugins {
    java
    idea
    id("multiloader-common")
}

stonecutterBuild.constants["fabric"] = targetLoader == "fabric"
stonecutterBuild.constants["neoforge"] = targetLoader == "neoforge"

evaluationDependsOn(commonProject.path)
val commonSourceSets = commonProject.extensions.getByType<SourceSetContainer>()
val commonMain = commonSourceSets.named("main")
val targetMain = rootProject.file("versions/${stonecutterBuild.current.version}/src/main")

sourceSets.main {
    java.srcDir(targetMain.resolve("java"))
    resources.srcDir(targetMain.resolve("resources"))
}

tasks.compileJava {
    source(commonMain.map { it.java })
}

tasks.processResources {
    from(commonMain.map { it.resources })

    val values = mutableMapOf<String, Any>(
        "version" to project.version,
        "group" to project.group,
        "minecraft_version" to minecraftVersion,
        "minecraft_version_range" to requiredProperty("minecraft_version_range"),
        "mod_name" to requiredProperty("mod_name"),
        "mod_author" to requiredProperty("mod_author"),
        "mod_id" to requiredProperty("mod_id"),
        "license" to requiredProperty("license"),
        "description" to project.description.orEmpty(),
        "credits" to requiredProperty("credits"),
        "java_version" to requiredProperty("java_version")
    )
    if (targetLoader == "fabric") {
        values["fabric_version"] = requiredProperty("fabric_version")
        values["fabric_loader_version"] = requiredProperty("fabric_loader_version")
    } else {
        values["neoforge_version"] = requiredProperty("neoforge_version")
        values["neoforge_loader_version_range"] = requiredProperty("neoforge_loader_version_range")
    }
    filesMatching(listOf(
        "pack.mcmeta",
        "fabric.mod.json",
        "META-INF/mods.toml",
        "META-INF/neoforge.mods.toml",
        "*.mixins.json"
    )) {
        expand(values)
    }
    inputs.properties(values)
}

base {
    archivesName = "Totem-Doll-${targetLoader}-${minecraftVersion}"
}
