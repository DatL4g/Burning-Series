import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktorfit)
}

val version = appVersion
val artifact = VersionCatalog.artifactName("extension")
group = artifact

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain.get().dependencies {
            runtimeOnly(npm("webextension-polyfill", "0.10.0"))
        }
    }
}

tasks {
    val extensionFolder = File(rootProject.layout.buildDirectory.get().asFile, "extension")
    val resourcesFolder = File(projectDir, "src/jsMain/resources")
    val releaseFolder = File(rootProject.layout.buildDirectory.get().asFile, "release/main/extension")
    val commonShared = File(rootProject.project("app").project("shared").projectDir, "src/commonMain")
    val iconsFolder = File(commonShared, "resources/MR/assets/png")
    var firefox = false

    val buildAndCopy = register("buildAndCopy") {
        dependsOn(
            project("content").tasks.build,
            project("background").tasks.build,
            assemble
        )

        doLast {
            copy {
                from(File(layout.buildDirectory.get().asFile, "distributions")) {
                    include("*.js")
                }
                into(extensionFolder)
            }

            copy {
                from(File(resourcesFolder, "manifest.json"))
                into(extensionFolder)

                from(iconsFolder) {
                    include("launcher_*.png")
                    into("icons")
                }
            }

            copy {
                val buildDirs = listOf(
                    rootProject.layout.buildDirectory.get().asFile,
                    layout.buildDirectory.get().asFile
                )

                from(buildDirs.map { File(it, "js/node_modules/webextension-polyfill/dist") }) {
                    include("browser-polyfill.min.js")
                    include("browser-polyfill.min.js.map")
                }
                into(extensionFolder)
            }

            jsonObjectAsMap(
                JsonSlurper().parse(File(extensionFolder, "manifest.json"))
            )?.toMutableMap()?.let { json ->
                if (json.containsKey("version")) {
                    json["version"] = version
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
        archiveBaseName.set("Chromium-$version")
        destinationDirectory.set(releaseFolder)
    }
    register<Zip>("packFirefox") {
        firefox = true
        dependsOn(buildAndCopy)

        mkdir(releaseFolder)
        from(extensionFolder)
        archiveBaseName.set("Firefox-$version")
        archiveExtension.set("xpi")
        destinationDirectory.set(releaseFolder)
    }
}

fun jsonObjectAsMap(data: Any?): Map<String, Any>? {
    if (data == null) {
        return null
    }

    val map = data as? Map<*, *> ?: return null
    return runCatching {
        map as Map<String, Any>
    }.getOrNull()
}