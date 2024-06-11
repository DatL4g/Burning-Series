rootProject.name = "Burning-Series"
include(":composeApp", ":composeApp:sekret")
include(":settings")
include(":model")
include(":network")
include(":firebase")
include(":database")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://jogamp.org/deployment/maven")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
