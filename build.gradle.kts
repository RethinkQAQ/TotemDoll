group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()
description = providers.gradleProperty("description").get()

val platformTargets = listOf(
    ":fabric:1.21.1",
    ":neoforge:1.21.1",
    ":fabric:1.21.4",
    ":neoforge:1.21.4"
)

val licenseSourceTargets = listOf(
    ":common:1.21.1",
    ":fabric:1.21.1",
    ":neoforge:1.21.1"
)

val formatLicenses = tasks.register("licenseFormat") {
    group = "formatting"
    description = "Applies LGPL-3.0 headers to all source modules."
}

val checkLicenses = tasks.register("licenseCheck") {
    group = "verification"
    description = "Checks LGPL-3.0 headers in all source modules."
}

gradle.projectsEvaluated {
    tasks.named("build") {
        dependsOn(platformTargets.map { project(it).tasks.named("build") })
    }
    formatLicenses.configure {
        dependsOn(licenseSourceTargets.map { project(it).tasks.named("licenseFormat") })
    }
    checkLicenses.configure {
        dependsOn(licenseSourceTargets.map { project(it).tasks.named("license") })
    }
    allprojects.forEach { candidate ->
        candidate.tasks.findByName("runServer")?.apply {
            enabled = false
            group = null
            description = "Disabled: Totem Doll is a client-only mod."
        }
    }
}

allprojects {
    tasks.matching { it.name == "runServer" }.configureEach {
        enabled = false
        group = null
        description = "Disabled: Totem Doll is a client-only mod."
    }
}
