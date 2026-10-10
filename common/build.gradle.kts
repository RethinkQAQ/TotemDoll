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
