import org.gradle.api.Plugin
import org.gradle.api.Project

class FabricLoomCompatPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        if (commonMod.unobfuscated) {
            setupUnobfuscatedLoom()
        } else {
            plugins.apply("net.fabricmc.fabric-loom-remap")
        }

        Unit
    }

    private fun Project.setupUnobfuscatedLoom() {
        plugins.apply("net.fabricmc.fabric-loom")

        listOf("api", "implementation", "compileOnly", "runtimeOnly", "localRuntime").forEach { name ->
            val modName = "mod" + name.replaceFirstChar(Char::uppercaseChar)
            val modConfiguration = configurations.findByName(modName) ?: configurations.create(modName)

            configurations.getByName(name).extendsFrom(modConfiguration)
        }

        configurations.findByName("mappings") ?: configurations.register("mappings") {
            isCanBeResolved = false
            isCanBeConsumed = false
        }
    }
}
