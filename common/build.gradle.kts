import org.gradle.api.attributes.Attribute

plugins {
    id("multiloader-common")
    id("fabric-loom-compat")
}

dependencies {
    minecraft("com.mojang:minecraft:${commonMod.mc}")
    if (stonecutter.eval(commonMod.mc, ">=26.3")) {
        compileOnly("org.lwjgl:lwjgl-tinyfd:${commonMod.dep("lwjgl-tinyfd")}")
    }
    if (stonecutter.eval(commonMod.mc, "<=1.21.11")) {
        mappings(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${commonMod.mc}:${commonMod.dep("parchment")}@zip")
        })
    }
    val rcui = "com.github.RethinkQAQ.RethinkConfigUiLib:rethink-config-ui-lib-${commonMod.mc}-fabric:${commonMod.prop("mod.rcui")}"
    modCompileOnly(rcui) { isTransitive = false }
    annotationProcessor(rcui) { isTransitive = false }
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

val commonSourceKind = Attribute.of("com.rethinkqaq.totemdoll.source-kind", String::class.java)
val commonJava = configurations.consumable("commonJava") {
    attributes.attribute(commonSourceKind, "java")
}
val commonResources = configurations.consumable("commonResources") {
    attributes.attribute(commonSourceKind, "resources")
}

artifacts {
    add(commonJava.name, stonecutterGeneratedMain.map { it.dir("java") }) {
        builtBy(stonecutterGenerateTask)
    }
    add(commonResources.name, stonecutterGeneratedMain.map { it.dir("resources") }) {
        builtBy(stonecutterGenerateTask)
    }
    // RCUI's processor generates wrappers consumed by both loader compilers.
    add(commonJava.name, tasks.compileJava.flatMap { it.options.generatedSourceOutputDirectory }) {
        builtBy(tasks.compileJava)
    }
}
