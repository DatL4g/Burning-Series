import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

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
    runtimeOnly(npm("webextension-polyfill", "0.10.0"))
    runtimeOnly(npm("@jaames/iro", "5.5.0"))
}

tasks {
    val extensionFolder = File(rootProject.buildDir, "extension")
    val resourcesFolder = File(projectDir, "src/main/resources")
    val assetsFolder = File(rootProject.project("app").projectDir, "src/commonMain/assets")
    val iconsFolder = File(assetsFolder, "png")
    val svgFolder = File(assetsFolder, "svg")
    val releaseFolder = File(rootProject.buildDir, "release/main/extension")

    val buildAndCopy = register("buildAndCopy") {
        dependsOn(
            project("content").tasks.build,
            project("background").tasks.build,
            project("popup").tasks.build,
            assemble
        )

        doLast {
            copy {
                from(File(buildDir, "distributions")) {
                    include("*.js")
                }
                into(extensionFolder)
            }

            copy {
                from(File(resourcesFolder, "manifest.json"))
                from(File(resourcesFolder, "html")) {
                    include("*.html")
                }
                from(File(resourcesFolder, "css")) {
                    include("*.css")
                }
                into(extensionFolder)

                from(iconsFolder) {
                    include("launcher_*.png")
                    into("icons")
                }
                from(svgFolder) {
                    include("GitHub.svg")
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
                from(buildDirs.map { File(it, "js/node_modules/@jaames/iro/dist") }) {
                    include("iro.min.js")
                }
                into(extensionFolder)
            }

            jsonObjectAsMap(
                JsonSlurper().parse(File(extensionFolder, "manifest.json"))
            )?.toMutableMap()?.let { json ->
                if (json.containsKey("version")) {
                    json["version"] = rootProject.project("app").version.toString()
                }

                File(extensionFolder, "manifest.json").writeText(JsonBuilder(json).toPrettyString())
            }
        }
    }

    register<Zip>("pack") {
        dependsOn(buildAndCopy)

        mkdir(releaseFolder)
        from(extensionFolder)
        archiveBaseName.set("BrowserExtension-${rootProject.project("app").version}")
        destinationDirectory.set(releaseFolder)
    }
}

@Suppress("UNCHECKED_CAST")
fun jsonObjectAsMap(data: Any?): Map<String, Any>? {
    if (data == null) {
        return null
    }

    val map = data as? Map<*, *> ?: return null
    return runCatching {
        map as Map<String, Any>
    }.getOrNull()
}
