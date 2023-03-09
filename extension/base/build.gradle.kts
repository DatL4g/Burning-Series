plugins {
    kotlin("js")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.4")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            useCommonJs()
            webpackTask {
                outputFileName = "base.js"
                sourceMaps = false
            }
            distribution {
                directory = parent?.buildDir?.let { File(it, "distributions") } ?: File(projectDir, "../build/distributions")
            }
        }
    }
}