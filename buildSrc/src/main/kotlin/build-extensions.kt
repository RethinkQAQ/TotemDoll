import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import dev.kikugie.stonecutter.controller.StonecutterControllerExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.*

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()

fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

val Project.stonecutterBuild get() = extensions.getByType<StonecutterBuildExtension>()
val Project.stonecutterController get() = extensions.getByType<StonecutterControllerExtension>()

val Project.common get() = requireNotNull(stonecutterBuild.node.sibling("common")) {
    "No common project for $project"
}
val Project.commonProject get() = rootProject.project(stonecutterBuild.current.project)
val Project.commonMod get() = commonProject.mod

val Project.loader: String? get() = prop("loader")

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = modProp("id")
    val name: String get() = modProp("name")
    val version: String get() = modProp("version")
    val group: String get() = modProp("group")
    val author: String get() = modProp("author")
    val description: String get() = modProp("description")
    val license: String get() = modProp("license")
    val github: String get() = modProp("github")
    val mc: String get() = depOrNull("minecraft") ?: project.stonecutterBuild.current.version
    val unobfuscated: Boolean
        get() {
            val parts = mc.substringBefore('-').split('.')
            val major = parts.getOrNull(0)?.toIntOrNull() ?: return false
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: return false
            return major > 26 || major == 26 && minor >= 1
        }

    fun propOrNull(key: String) = project.prop(key)
    fun prop(key: String) = requireNotNull(propOrNull(key)) { "Missing '$key'" }
    fun modPropOrNull(key: String) = project.prop("mod.$key")
    fun modProp(key: String) = requireNotNull(modPropOrNull(key)) { "Missing 'mod.$key'" }
    fun depOrNull(key: String) = project.prop("deps.$key")?.takeIf(String::isNotEmpty)
    fun dep(key: String) = requireNotNull(depOrNull(key)) { "Missing 'deps.$key'" }
}
