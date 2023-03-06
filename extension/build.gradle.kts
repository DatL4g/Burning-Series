plugins {
    kotlin("js")
    kotlin("plugin.serialization")
    id("de.jensklingenberg.ktorfit")
}

group = "dev.datlag.burningseries.extension"

kotlin {
    js(IR) {
        browser()
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":network"))
    
    runtimeOnly(npm("webextension-polyfill", "0.10.0"))
}

tasks {
    val extensionFolder = File(rootProject.buildDir, "extension")
    val resourcesFolder = File(projectDir, "src/main/resources")
    val iconsFolder = File(rootProject.project("app").projectDir, "src/commonMain/assets/png")
    val releaseFolder = File(rootProject.buildDir, "release/main/extension")

    val buildAndCopy = register("buildAndCopy") {
        dependsOn(rootProject.tasks.clean, assemble)
        doLast {
            copy {
                from(File(buildDir, "distributions")) {
                    include("*.js")
                }
                into(extensionFolder)
            }

            copy {
                into(extensionFolder)

                from(File(resourcesFolder, "manifest.json"))

                from(iconsFolder) {
                    include("launcher_*.png")
                    into("icons")
                }
            }

            copy {
                val buildDirs = listOf(
                    rootProject.buildDir,
                    buildDir
                )

                from(buildDirs.map { File(it, "js/node_modules/webextension-polyfill/dist") }) {
                    include("browser-polyfill.min.js")
                    include("browser-polyfill.min.js.map")
                }
                into(extensionFolder)
            }
        }
    }

    val pack = register<Zip>("pack") {
        dependsOn(buildAndCopy)

        mkdir(releaseFolder)
        from(extensionFolder)
        archiveBaseName.set("extension-${rootProject.project("app").version}")
        destinationDirectory.set(releaseFolder)
    }
}
