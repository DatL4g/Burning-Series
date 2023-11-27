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

    val buildAndCopy = register("buildAndCopy") {
        dependsOn(
            project("content").tasks.build,
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

                File(extensionFolder, "manifest.json").writeText(JsonBuilder(json).toPrettyString())
            }
        }
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