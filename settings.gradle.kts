rootProject.name = "Burning-Series"

include(":app:shared")
include(":app:shared:sekret")
include(":app:android")
include(":app:desktop")

include(":model")
include(":network")
include(":database")

include(
    ":extension",
    ":extension:base",
    ":extension:content",
    ":extension:background",
    ":extension:background:sekret",
)

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven("https://jogamp.org/deployment/maven")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.7.0")
}