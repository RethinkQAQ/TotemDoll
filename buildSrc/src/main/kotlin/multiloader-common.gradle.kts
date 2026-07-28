plugins {
    `java-library`
    `maven-publish`
    idea
}

group = requiredProperty("group")
version = requiredProperty("version")
description = requiredProperty("description")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(requiredProperty("java_version").toInt())
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://repo.spongepowered.org/repository/maven-public") {
                name = "Sponge"
            }
        }
        filter { includeGroupAndSubgroups("org.spongepowered") }
    }
    exclusiveContent {
        forRepositories(
            maven("https://maven.parchmentmc.org/") { name = "ParchmentMC" },
            maven("https://maven.neoforged.net/releases") { name = "NeoForge" }
        )
        filter { includeGroup("org.parchmentmc.data") }
    }
    maven("https://maven.blamejared.com") { name = "BlameJared" }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${requiredProperty("mod_name")}" }
    }
}
