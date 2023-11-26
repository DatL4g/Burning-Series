plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktorfit)
}

val artifact = VersionCatalog.artifactName("extension")
group = artifact

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain.get().dependencies {
            runtimeOnly(npm("webextension-polyfill", "0.10.0"))
        }
    }
}