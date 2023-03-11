plugins {
    kotlin("js")
    kotlin("plugin.serialization")
}

dependencies {
    parent?.project("base")?.let { implementation(it) }
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                outputFileName = "popup.js"
                sourceMaps = false
            }
            distribution {
                directory = parent?.buildDir?.let { File(it, "distributions") } ?: File(projectDir, "../build/distributions")
            }
        }
    }
}