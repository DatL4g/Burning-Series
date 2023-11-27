plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.sekret)
}

sekret {
    packageName = "dev.datlag.burningseries"
    generateJsSourceSet = true
    propertiesFile = rootDir.canonicalPath
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                mainOutputFileName = "background.js"
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
            implementation(rootProject.project("network"))
        }
    }
}