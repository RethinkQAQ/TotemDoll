import java.util.Properties

private fun String.xmlEscape() = replace("&", "&amp;").replace("\"", "&quot;")
    .replace("'", "&apos;").replace("<", "&lt;").replace(">", "&gt;")

val isCi = System.getenv("CI") == "true"
gradle.startParameter.isParallelProjectExecutionEnabled = !isCi
gradle.startParameter.isBuildCacheEnabled = true
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
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val supportedVersions = providers.gradleProperty("stonecutter_enabled_versions")
    .get().split(',').map(String::trim).filter(String::isNotEmpty)
val allowedPlatforms = setOf("fabric", "neoforge")
check(supportedVersions.isNotEmpty()) { "stonecutter_enabled_versions is empty" }
check(supportedVersions.size == supportedVersions.toSet().size) { "Duplicate Minecraft versions" }
check(supportedVersions.all { it.matches(Regex("\\d+\\.\\d+(?:\\.\\d+)?")) }) {
    "Invalid Minecraft version in stonecutter_enabled_versions"
}

val versionMetadata = supportedVersions.associateWith { version ->
    Properties().apply {
        val source = file("versions/$version/gradle.properties")
        check(source.isFile) { "Missing version properties: $source" }
        source.inputStream().use(::load)
    }
}
val enabledPlatformsByVersion = versionMetadata.mapValues { (version, values) ->
    val platforms = values.getProperty("enable_platforms", "").split(',')
        .map(String::trim).filter(String::isNotEmpty)
    check(platforms.isNotEmpty() && platforms.size == platforms.toSet().size) {
        "Version $version has empty or duplicate enable_platforms"
    }
    check(platforms.all { it in allowedPlatforms }) { "Unsupported platforms for $version: $platforms" }
    platforms.toSet()
}
fun validateVersionMetadata() {
    versionMetadata.forEach { (version, values) ->
        val prefix = "versions/$version/gradle.properties"
        listOf("minecraft_version", "publish_version", "java.version", "resource_pack_format").forEach { key ->
            check(!values.getProperty(key).isNullOrBlank()) { "$prefix is missing $key" }
        }
        check(values.getProperty("minecraft_version") == version) { "$prefix must declare minecraft_version=$version" }
        val expectedJava = if (version.substringBefore('.').toInt() >= 26) 25 else 21
        check(values.getProperty("java.version").toIntOrNull() == expectedJava) {
            "$prefix must use java.version=$expectedJava"
        }
        check(values.getProperty("publish_version").split(',').all {
            it.trim().matches(Regex("\\d+\\.\\d+(?:\\.(?:\\d+|x))?"))
        }) { "$prefix has invalid publish_version" }
        check(values.getProperty("resource_pack_format").matches(Regex("\\d+(?:\\.\\d+)?"))) {
            "$prefix has invalid resource_pack_format"
        }
        enabledPlatformsByVersion.getValue(version).forEach { platform ->
            check(!values.getProperty("minecraft_version_range_$platform").isNullOrBlank()) {
                "$prefix is missing minecraft_version_range_$platform"
            }
        }
    }
}
validateVersionMetadata()
val platformVersions = allowedPlatforms.associateWith { platform ->
    supportedVersions.filter { platform in enabledPlatformsByVersion.getValue(it) }
}
val vcsBaselineVersion = "1.21.1"
check(vcsBaselineVersion in supportedVersions) { "The VCS baseline $vcsBaselineVersion must stay registered" }

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        versions(*supportedVersions.toTypedArray())
        vcsVersion = vcsBaselineVersion
        branch("common") { versions(*supportedVersions.toTypedArray()) }
        platformVersions.forEach { (platform, versions) ->
            if (versions.isNotEmpty()) branch(platform) { versions(*versions.toTypedArray()) }
        }
    }
}
rootProject.name = "TotemDoll"

gradle.projectsLoaded {
    rootProject.group = providers.gradleProperty("mod.group").get()
    rootProject.version = providers.gradleProperty("mod.version").get()
    val validateVersionProperties = rootProject.tasks.register("validateVersionProperties") {
        group = "verification"
        inputs.files(supportedVersions.map { rootProject.file("versions/$it/gradle.properties") })
        doLast { validateVersionMetadata() }
    }
    val loaderTaskPaths = platformVersions.flatMap { (platform, versions) ->
        versions.map { ":$platform:$it:build" }
    }
    val licenseTaskPaths = supportedVersions.map { ":common:$it:licenseCheck" } +
        platformVersions.flatMap { (platform, versions) -> versions.map { ":$platform:$it:licenseCheck" } }
    val rootBuild = rootProject.tasks.register("build") {
        group = "build"
        description = "Builds every registered loader and Minecraft version."
        dependsOn(loaderTaskPaths, validateVersionProperties)
    }
    val jarVersion = providers.gradleProperty("mod.version").get()
    val jarSuffix = providers.gradleProperty("build.number").orNull?.trim()?.takeIf(String::isNotEmpty)
    val modId = providers.gradleProperty("mod.id").get()
    val expectedJars = platformVersions.flatMap { (platform, versions) ->
        versions.map { version ->
            val name = "$modId-$platform-$jarVersion-mc$version${jarSuffix?.let { "-$it" } ?: ""}.jar"
            rootProject.project(":$platform:$version").layout.buildDirectory.file("libs/$name")
        }
    }
    val validateJarOutputs = rootProject.tasks.register("validateJarOutputs") {
        group = "verification"
        dependsOn(rootBuild)
        doLast {
            expectedJars.forEach { jar -> check(jar.get().asFile.isFile) { "Missing publishable JAR: ${jar.get().asFile}" } }
        }
    }
    rootProject.tasks.register("collectJars", Copy::class.java) {
        group = "build"
        dependsOn(validateJarOutputs)
        into(rootProject.layout.buildDirectory.dir("mod-jars"))
        duplicatesStrategy = DuplicatesStrategy.FAIL
        val outputDirectory = rootProject.layout.buildDirectory.dir("mod-jars")
        doFirst { rootProject.delete(outputDirectory) }
        from(expectedJars)
    }
    rootProject.tasks.register("licenseFormat") {
        group = "formatting"
        dependsOn(licenseTaskPaths.map { it.replace(":licenseCheck", ":licenseFormat") })
    }
    rootProject.tasks.register("licenseCheck") {
        group = "verification"
        dependsOn(licenseTaskPaths)
    }

    val runClient = providers.gradleProperty("run.client").map(String::toBoolean).orElse(true)
    val runServer = providers.gradleProperty("run.server").map(String::toBoolean).orElse(false)
    val ideDirectory = rootProject.layout.projectDirectory.dir(".idea/runConfigurations")
    val generateIdeRuns = rootProject.tasks.register("generateIdeGradleRunConfigurations") {
        group = "ide"
        inputs.property("run.client", runClient)
        inputs.property("run.server", runServer)
        inputs.property("platformVersions", platformVersions.mapValues { it.value.joinToString(",") })
        outputs.dir(ideDirectory)
        doLast {
            val directory = ideDirectory.asFile
            check(directory.exists() || directory.mkdirs()) { "Cannot create $directory" }
            directory.listFiles { file -> file.name.startsWith("Stonecutter_Gradle_") && file.extension == "xml" }
                ?.forEach { check(it.delete()) { "Cannot remove stale generated configuration: $it" } }
            platformVersions.forEach { (platform, versions) ->
                versions.forEach { version ->
                    listOf("Client" to runClient.get(), "Server" to runServer.get()).filter { it.second }.forEach { (side, _) ->
                        val displayPlatform = if (platform == "neoforge") "NeoForge" else "Fabric"
                        val task = ":$platform:$version:run$side"
                        directory.resolve("Stonecutter_Gradle_${platform}_${version.replace('.', '_')}_${side.lowercase()}.xml")
                            .writeText("""
                                |<component name="ProjectRunConfigurationManager">
                                |  <configuration default="false" factoryName="Gradle" name="${("$displayPlatform $side $version (Gradle)").xmlEscape()}" type="GradleRunConfiguration">
                                |    <ExternalSystemSettings>
                                |      <option name="externalProjectPath" value="${'$'}PROJECT_DIR${'$'}" />
                                |      <option name="externalSystemIdString" value="GRADLE" />
                                |      <option name="taskNames"><list><option value="${task.xmlEscape()}" /></list></option>
                                |    </ExternalSystemSettings>
                                |    <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
                                |    <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
                                |    <DebugAllEnabled>false</DebugAllEnabled>
                                |    <RunAsTest>false</RunAsTest>
                                |    <GradleCoverageDisabled>false</GradleCoverageDisabled>
                                |    <method v="2" />
                                |  </configuration>
                                |</component>
                            """.trimMargin() + System.lineSeparator())
                    }
                }
            }
        }
    }
    // Stonecutter recreates runConfigurations; write our launchers afterwards.
    rootProject.tasks.matching { it.name == "stonecutterIdea" }.configureEach { finalizedBy(generateIdeRuns) }
}
