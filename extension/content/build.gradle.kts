plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                mainOutputFileName = "content_script.js"
                sourceMaps = false
            }
            distribution {
                outputDirectory.set(parentBuildDir("distributions"))
            }
        }
    }

    sourceSets {
        jsMain.get().dependencies {
            implementation(parent?.project("base") ?: rootProject.project("extension:base"))
            implementation(rootProject.project("model"))
            implementation(rootProject.project("color"))
        }
    }
}