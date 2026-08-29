plugins {
    id("multiloader-common")
    id("fabric-loom-compat")
    id("com.github.hierynomus.license") version "0.16.1"
}

apply(from = rootProject.file("license.gradle"))

tasks.named("compileJava") {
    dependsOn("licenseFormat")
}

tasks.named("licenseMain") {
    dependsOn("licenseFormat")
}

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (stonecutter.eval(commonMod.mc, "<=1.21.11")) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:${commonMod.dep("parchment")}@zip")
        })
    }
    // Common is compiled with Fabric mappings; use the matching RCUI adapter.
    val rcui = "${commonMod.prop("rcui.group")}:${commonMod.prop("rcui.artifact_base")}-fabric:${commonMod.mc}-${commonMod.prop("rcui.version")}"
    // Common only consumes RCUI API and the annotation processor; loader projects
    // provide and embed the runtime platform artifact.
    modImplementation(rcui) { isTransitive = false }
    val rcuiConfig = "${commonMod.prop("rcui.group")}:rethink-config-ui-lib-config:${commonMod.prop("rcui.version")}"
    annotationProcessor(rcuiConfig) { isTransitive = false }
}

configureTotemDollMixinSupport(TotemDollMixinTarget.COMMON)

if (!commonMod.unobfuscated) {
    loom {
        mixin {
            useLegacyMixinAp.set(true)
            defaultRefmapName.set("${commonMod.id}.refmap.json")
        }
    }
}

val commonJava: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}
val commonResources: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    afterEvaluate {
        val main = sourceSets.main.get()
        main.java.sourceDirectories.files.forEach { add(commonJava.name, it) }
        main.resources.sourceDirectories.files.forEach { add(commonResources.name, it) }

        // Annotation processors write generated wrappers outside the regular
        // source directories. Export that directory as common Java source so
        // loader projects compile and package the generated configuration API.
        add(commonJava.name, layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main"))
    }
}
