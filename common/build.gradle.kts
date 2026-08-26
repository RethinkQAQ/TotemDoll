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
    compileOnly("com.github.RethinkQAQ.RethinkConfigUiLib:rethink-config-ui-lib-mc${commonMod.mc}:0.1.2-local")
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
    }
}
