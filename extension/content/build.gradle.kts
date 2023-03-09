plugins {
    kotlin("js")
    kotlin("plugin.serialization")
}

dependencies {
    parent?.project("base")?.let { implementation(it) }
    implementation(rootProject.project("model"))
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                outputFileName = "content_script.js"
                sourceMaps = false
            }
            distribution {
                directory = parent?.buildDir?.let { File(it, "distributions") } ?: File(projectDir, "../build/distributions")
            }
        }
    }
}