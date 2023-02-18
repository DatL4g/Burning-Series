// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.8.10" apply false
    kotlin("plugin.serialization") version "1.8.10" apply false
    kotlin("android") version "1.8.0" apply false
    id("org.jetbrains.compose") version "1.3.0" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("com.google.protobuf") version "0.9.2" apply false
    id("com.squareup.sqldelight") version "1.5.5" apply false
    id("com.mikepenz.aboutlibraries.plugin") version "10.5.2" apply false
    id("de.jensklingenberg.ktorfit") version "1.0.0" apply false
    id("com.github.ben-manes.versions") version "0.45.0"
}

buildscript {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://packages.jetbrains.team/maven/p/ui/dev") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")

        // wait for https://github.com/Faire/gradle-kotlin-buildozer/pull/13
    }
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://packages.jetbrains.team/maven/p/ui/dev") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}