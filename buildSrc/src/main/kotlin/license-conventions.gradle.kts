import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withGroovyBuilder
import java.time.Year

plugins {
    id("com.github.hierynomus.license")
}

val licenseFormat = tasks.named("licenseFormat")
tasks.named("licenseMain") {
    dependsOn(licenseFormat)
}
tasks.register("licenseCheck") {
    group = "verification"
    dependsOn("licenseMain")
}
tasks.withType<JavaCompile>().configureEach {
    dependsOn(licenseFormat)
}

extensions.getByName("license").withGroovyBuilder {
    setProperty("header", rootProject.file("HEADER.txt"))
    setProperty("skipExistingHeaders", true)
    setProperty("ignoreFailures", false)
    "include"("**/*.java")

    "headerDefinitions" {
        "create"("SLASHSTAR_STYLE_NEWLINE") {
            setProperty("firstLine", "/*")
            setProperty("beforeEachLine", " * ")
            setProperty("endLine", " */" + System.lineSeparator())
            setProperty("afterEachLine", "")
            setProperty("skipLinePattern", null)
            setProperty("firstLineDetectionPattern", "(\\s|\\t)*/\\*.*$")
            setProperty("lastLineDetectionPattern", ".*\\*/(\\s|\\t)*$")
            setProperty("allowBlankLines", false)
            setProperty("isMultiline", true)
            setProperty("padLines", false)
        }
    }
    "mapping" {
        setProperty("java", "SLASHSTAR_STYLE_NEWLINE")
    }
    "ext" {
        setProperty("name", providers.gradleProperty("mod.name").get())
        setProperty("author", "Rethink_QAQ")
        setProperty("year", Year.now().value.toString())
    }
}
