import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

val Project.stonecutterBuild: StonecutterBuildExtension
    get() = extensions.getByType()

val Project.commonNode
    get() = requireNotNull(stonecutterBuild.node.sibling("common")) {
        "No common Stonecutter project for $path"
    }

val Project.commonProject: Project
    get() = rootProject.project(":common:${stonecutterBuild.current.version}")

val Project.versionProject: Project
    get() = rootProject.project(stonecutterBuild.current.project)

fun Project.requiredProperty(name: String): String =
    requireNotNull(
        versionProject.findProperty(name)?.toString()
            ?: findProperty(name)?.toString()
            ?: rootProject.findProperty(name)?.toString()
    ) { "Missing Gradle property '$name' in $path" }

val Project.minecraftVersion: String
    get() = requiredProperty("minecraft_version")

val Project.targetLoader: String
    get() = when {
        path.startsWith(":fabric:") -> "fabric"
        path.startsWith(":neoforge:") -> "neoforge"
        else -> error("Unknown Totem Doll loader for $path")
    }
