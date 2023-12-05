plugins {
    alias(libs.plugins.multiplatform)
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            useCommonJs()
            webpackTask {
                mainOutputFileName.set("base.js")
                sourceMaps = false
            }
            distribution {
                outputDirectory.set(parentBuildDir("distributions"))
            }
        }
    }

    sourceSets {
        dependencies {

        }
    }
}