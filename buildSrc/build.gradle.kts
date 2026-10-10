plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.kikugie.dev/releases")
}

dependencies {
    implementation("dev.kikugie:stonecutter:0.7.11")
    implementation("com.github.hierynomus.license:com.github.hierynomus.license.gradle.plugin:0.16.1")
}
