val isCi = System.getenv("CI") == "true"
gradle.startParameter.isParallelProjectExecutionEnabled = !isCi
gradle.startParameter.isBuildCacheEnabled = !isCi
gradle.startParameter.isConfigureOnDemand = !isCi

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.11"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val supportedVersions = providers.gradleProperty("stonecutter_enabled_versions")
    .get().split(",").map(String::trim).filter(String::isNotEmpty)

fun enabledPlatforms(version: String): Set<String> {
    val properties = java.util.Properties()
    val propertiesFile = file("versions/$version/gradle.properties")
    check(propertiesFile.isFile) {
        "Missing version properties file: ${propertiesFile.path}"
    }
    propertiesFile.inputStream().use(properties::load)
    return properties.getProperty("enable_platforms")
        ?.split(",")
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.toSet()
        ?: error("Version $version is missing enable_platforms in ${propertiesFile.path}")
}

val platformVersions = mapOf(
    "fabric" to supportedVersions.filter { "fabric" in enabledPlatforms(it) },
    "neoforge" to supportedVersions.filter { "neoforge" in enabledPlatforms(it) }
)

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions(*supportedVersions.toTypedArray())

        branch("common") {
            versions(*supportedVersions.toTypedArray())
        }

        platformVersions.forEach { (branchName, versions) ->
            branch(branchName) {
                versions(*versions.toTypedArray())
            }
        }
    }
}

rootProject.name = "TotemDoll"

gradle.projectsLoaded {
    rootProject.pluginManager.apply("base")
    rootProject.group = providers.gradleProperty("mod.group").get()
    rootProject.version = providers.gradleProperty("mod.version").get()
}

gradle.projectsEvaluated {
    val loaderProjects = rootProject.allprojects.filter {
        it.path.matches(Regex("^:(fabric|neoforge):[^:]+$"))
    }
    val licenseProjects = rootProject.allprojects.filter {
        it.path.matches(Regex("^:(common|fabric|neoforge):[^:]+$"))
    }

    rootProject.tasks.named("build") {
        dependsOn(loaderProjects.map { it.tasks.named("build") })
    }
    rootProject.tasks.register("licenseFormat") {
        group = "formatting"
        description = "Applies LGPL-3.0 headers to Java source files."
        dependsOn(licenseProjects.map { it.tasks.named("licenseFormat") })
    }
    rootProject.tasks.register("licenseCheck") {
        group = "verification"
        description = "Checks LGPL-3.0 headers in Java source files."
        dependsOn(licenseProjects.map { it.tasks.named("license") })
    }

    rootProject.tasks.named("build") {
        dependsOn(rootProject.tasks.named("licenseFormat"))
    }
}
