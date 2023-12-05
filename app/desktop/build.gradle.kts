import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.osdetector)
}

val artifact = VersionCatalog.artifactName()

group = artifact
version = appVersion

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":app:shared"))
            }
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "$artifact.MainKt"

            when (getHost()) {
                Host.Linux -> {
                    jvmArgs("--add-opens", "java.desktop/sun.awt.X11=ALL-UNNAMED")
                    jvmArgs("--add-opens", "java.desktop/sun.awt.wl=ALL-UNNAMED")
                }
                Host.MAC -> {
                    jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
                    jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
                    jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
                }
                else -> { }
            }
            jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

            nativeDistributions {
                packageName = "Burning-Series"
                packageVersion = appVersion
                outputBaseDir.set(rootProject.layout.buildDirectory.asFile.get().resolve("release"))
                description = "Watch any series from Burning-Series using this (unofficial) app."
                copyright = "© 2020 Jeff Retz (DatLag). All rights reserved."
                licenseFile.set(rootProject.file("LICENSE"))

                outputBaseDir.set(rootProject.layout.buildDirectory.asFile.get().resolve("release"))
                appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

                when (getHost()) {
                    Host.Linux -> targetFormats(
                        TargetFormat.AppImage, TargetFormat.Deb, TargetFormat.Rpm
                    )
                    Host.MAC -> targetFormats(
                        TargetFormat.Dmg, TargetFormat.Pkg
                    )
                    Host.Windows -> targetFormats(
                        TargetFormat.Exe, TargetFormat.Msi
                    )
                }

                linux {
                    rpmLicenseType = "GPL-3.0"
                    debMaintainer = "Jeff Retz (DatLag)"
                    appCategory = "Video"
                }
                windows {
                    upgradeUuid = "3487d337-1ef5-4e01-87cb-d1ede6e10752"
                }
                macOS {

                }

                includeAllModules = true
            }
        }
    }
}

fun getHost(): Host {
    return when (osdetector.os) {
        "linux" -> Host.Linux
        "osx" -> Host.MAC
        "windows" -> Host.Windows
        else -> {
            val hostOs = System.getProperty("os.name")
            val isMingwX64 = hostOs.startsWith("Windows")

            when {
                hostOs == "Linux" -> Host.Linux
                hostOs == "Mac OS X" -> Host.MAC
                isMingwX64 -> Host.Windows
                else -> throw IllegalStateException("Unknown OS: ${osdetector.classifier}")
            }
        }
    }
}

enum class Host(val label: String) {
    Linux("linux"),
    Windows("win"),
    MAC("mac");
}