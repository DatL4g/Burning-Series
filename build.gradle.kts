import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files

plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.cocoapods) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktorfit) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.osdetector) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.realm) apply false
    alias(libs.plugins.sekret) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.complete.kotlin)
    alias(libs.plugins.versions)
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://jogamp.org/deployment/maven")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://plugins.gradle.org/m2/")
        maven("https://jogamp.org/deployment/maven")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = CompileOptions.jvmTarget
    }
}

plugins.withType<YarnPlugin> {
    yarn.yarnLockAutoReplace = true
}

tasks.withType<DependencyUpdatesTask> {
    outputFormatter {
        val updatable = this.outdated.dependencies
        val markdown = if (updatable.isEmpty()) {
            buildString {
                append("### Dependencies up-to-date")
                appendLine()
                appendLine()
                appendLine("Everything up-to-date")
                appendLine()
                appendLine("### Gradle Version")
                appendLine()
                appendLine("**Current version:** ${this@outputFormatter.gradle.running.version}")
                appendLine("**Latest version:** ${this@outputFormatter.gradle.current.version}")
            }
        } else {
            buildString {
                append("## Updatable dependencies (${updatable.size})")
                appendLine()
                appendLine()
                append('|')
                append("Group")
                append('|')
                append("Module")
                append('|')
                append("Used Version")
                append('|')
                append("Available Version")
                append('|')
                appendLine()
                append('|')
                repeat(2) {
                    append("---")
                    append('|')
                }
                repeat(2) {
                    append(":-:")
                    append('|')
                }
                updatable.forEach { dependency ->
                    appendLine()
                    append('|')
                    append(dependency.group ?: ' ')
                    append('|')
                    append(dependency.name ?: ' ')
                    append('|')
                    append(dependency.version ?: ' ')
                    append('|')
                    append(dependency.available.release ?: dependency.available.milestone ?: ' ')
                    append('|')
                }
                appendLine()
                appendLine()
                appendLine("### Gradle Version")
                appendLine()
                appendLine("**Current version:** ${this@outputFormatter.gradle.running.version}")
                appendLine("**Latest version:** ${this@outputFormatter.gradle.current.version}")
            }
        }
        val outputFile = layout.buildDirectory.file("dependencyUpdates/report.md").get().asFile
        try {
            if (outputFile.exists()) {
                outputFile.delete()
            }
        } catch (ignored: Throwable) { }
        try {
            outputFile.parentFile.mkdirs()
        } catch (ignored: Throwable) { }
        try {
            outputFile.writeText(markdown)
        } catch (ignored: Throwable) { }
    }
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.create("createSekretProperties") {
    var file = File(rootDir, "sekret.properties")
    val key = properties["key"] as? String ?: return@create
    val value = properties["value"] as? String ?: return@create

    val append = if (!file.existsSafely()) {
        file = file.create()
        false
    } else {
        true
    }

    if (append) {
        file.appendText(
            "\n$key=$value"
        )
    } else {
        file.writeText(
            "$key=$value"
        )
    }
}

fun File.existsSafely() = runCatching {
    Files.exists(this.toPath())
}.getOrNull() ?: runCatching {
    this.exists()
}.getOrNull() ?: false

fun File.create() = runCatching {
    Files.createFile(this.toPath()).toFile()
}.getOrNull() ?: runCatching {
    this.createNewFile()
    this
}.getOrNull() ?: this