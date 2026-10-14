import org.gradle.api.attributes.Attribute

plugins {
    java
    idea
    id("multiloader-common")
}

val commonPath = ":common:${commonMod.mc}"
val commonSourceKind = Attribute.of("com.rethinkqaq.totemdoll.source-kind", String::class.java)
val commonJavaDependencies = configurations.dependencyScope("commonJavaDependencies")
val commonResourcesDependencies = configurations.dependencyScope("commonResourcesDependencies")
val commonJava = configurations.resolvable("commonJava") {
    extendsFrom(commonJavaDependencies.get())
    attributes.attribute(commonSourceKind, "java")
}
val commonResources = configurations.resolvable("commonResources") {
    extendsFrom(commonResourcesDependencies.get())
    attributes.attribute(commonSourceKind, "resources")
}

dependencies {
    compileOnly(project(path = commonPath))
    add(commonJavaDependencies.name, project(commonPath))
    add(commonResourcesDependencies.name, project(commonPath))
}

tasks {
    compileJava {
        dependsOn(stonecutterGenerateTask)
        setSource(files(stonecutterGeneratedMain.map { it.dir("java") }, commonJava))
    }
    processResources {
        from(commonResources)
    }
}
