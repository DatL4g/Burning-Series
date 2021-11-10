// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
}

buildscript {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx/") }
        maven { url = uri("https://jitpack.io") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.31")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
        classpath("io.michaelrocks:paranoid-gradle-plugin:0.3.5")
    }
}

allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://kotlin.bintray.com/kotlinx/") }
        maven { url = uri("https://jitpack.io") }
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