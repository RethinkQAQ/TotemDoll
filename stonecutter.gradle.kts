plugins {
    id("dev.kikugie.stonecutter")
    base
}

apply(from = rootProject.file("build.gradle.kts"))

stonecutter.active("1.21.4")
