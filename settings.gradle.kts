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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

fun enabledVersions(property: String) = providers.gradleProperty(property).orNull
    ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()

val commonVersions = enabledVersions("stonecutter_enabled_common_versions")
val fabricVersions = enabledVersions("stonecutter_enabled_fabric_versions")
val neoForgeVersions = enabledVersions("stonecutter_enabled_neoforge_versions")
val distributions = linkedMapOf(
    "common" to commonVersions,
    "fabric" to fabricVersions,
    "neoforge" to neoForgeVersions
)
val allVersions = distributions.values.flatten().distinct()

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions(*allVersions.toTypedArray())
        distributions.forEach { (branchName, branchVersions) ->
            branch(branchName) {
                versions(*branchVersions.toTypedArray())
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
