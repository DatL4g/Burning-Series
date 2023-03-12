plugins {
    kotlin("js")
    kotlin("plugin.serialization")
    id("de.jensklingenberg.ktorfit")
}

val ktor = "2.2.3"

dependencies {
    parent?.project("base")?.let { implementation(it) }
    implementation(rootProject.project("model"))
    implementation(rootProject.project("network"))

    implementation("io.ktor:ktor-client-js:$ktor")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                outputFileName = "background.js"
                sourceMaps = false
            }
            distribution {
                directory = parent?.buildDir?.let { File(it, "distributions") } ?: File(projectDir, "../build/distributions")
            }
        }
    }
}