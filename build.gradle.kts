// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("multiplatform") version "1.7.10" apply false
    kotlin("plugin.serialization") version "1.7.10" apply false
    kotlin("android") version "1.7.10" apply false
    id("org.jetbrains.compose") version "1.2.1" apply false
    id("com.google.devtools.ksp") version "1.7.10-1.0.6" apply false
    id("com.google.protobuf") version "0.9.1" apply false
    id("com.github.ben-manes.versions") version "0.44.0"
}

buildscript {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")

        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.1")
        classpath("io.michaelrocks:paranoid-gradle-plugin:0.3.7")
        classpath("com.klaxit.hiddensecrets:HiddenSecretsPlugin:0.2.1")
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:10.5.1")

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

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}