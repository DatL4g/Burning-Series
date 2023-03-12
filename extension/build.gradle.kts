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
    val commonAppFolder = File(rootProject.project("app").projectDir, "src/commonMain")
    val assetsFolder = File(commonAppFolder, "assets")
    val iconsFolder = File(assetsFolder, "png")
    val svgFolder = File(assetsFolder, "svg")
    val fontFolder = File(commonAppFolder, "resources/font")
    val releaseFolder = File(rootProject.buildDir, "release/main/extension")
    var firefox = false

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
                from(fontFolder) {
                    include("manrope_regular.ttf")
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
                if (firefox) {
                    json["background"] = mapOf("scripts" to listOf("background.js"))
                    json["browser_specific_settings"] = mapOf("gecko" to mapOf("id" to "burningseries@datlag.dev"))

                    val permissions = ((json["permissions"] as? List<String>) ?: listOf("storage")).toMutableList()
                    permissions.remove("background")
                    permissions.add("activeTab")
                    json["permissions"] = permissions
                }

                File(extensionFolder, "manifest.json").writeText(JsonBuilder(json).toPrettyString())
            }
        }
    }

    register<Zip>("packChromium") {
        firefox = false
        dependsOn(buildAndCopy)

        mkdir(releaseFolder)
        from(extensionFolder)
        archiveBaseName.set("Chromium-${rootProject.project("app").version}")
        destinationDirectory.set(releaseFolder)
    }

    register<Zip>("packFirefox") {
        firefox = true
        dependsOn(buildAndCopy)

        mkdir(releaseFolder)
        from(extensionFolder)
        archiveBaseName.set("Firefox-${rootProject.project("app").version}")
        archiveExtension.set("xpi")
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
