rootProject.name = "Burning-Series"

include(":app:shared")
include(":app:android")
include(":app:desktop")

include(":model")
include(":network")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}
