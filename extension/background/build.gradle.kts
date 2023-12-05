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

            implementation(libs.ktor)
            implementation(libs.ktor.js)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }
    }
}

tasks.build {
    dependsOn(tasks.generateSekret)
}