import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.android.application")
    id("kotlin-parcelize") apply false
    id("com.mikepenz.aboutlibraries.plugin")
    id("de.jensklingenberg.ktorfit")
    id("com.google.osdetector")
}

val coroutines = "1.6.4"
val decompose = "1.0.0"
val kodein = "7.18.0"
val ktor = "2.2.3"
val exoplayer = "1.0.0-rc01"
val accompanist = "0.25.1"

val javafx = "19.0.2.1"
val javafxModules = listOf(
    "javafx.base",
    "javafx.graphics", // depends on base
    "javafx.controls", // depends on base & graphics
    "javafx.media", // depends on base & graphics
    "javafx.swing", // depends on base & graphics
    "javafx.web", // depends on base & graphics & controls & media
)

val artifact = "dev.datlag.burningseries"
val appVersion = "4.4.0"
val appCode = 440

group = artifact
version = appVersion

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android()
    jvm("desktop")

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("11"))
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)

                api("com.arkivanov.decompose:decompose:$decompose")
                api("com.arkivanov.decompose:extensions-compose-jetbrains:$decompose")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
                api("org.kodein.di:kodein-di:$kodein")
                implementation("org.kodein.di:kodein-di-framework-compose:$kodein")
                api("io.ktor:ktor-client-okhttp:$ktor")
                api("io.ktor:ktor-client-content-negotiation:$ktor")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor")
                api("com.squareup.okhttp3:okhttp-dnsoverhttps:4.10.0")

                implementation("com.mikepenz:aboutlibraries-compose:10.6.1")
                implementation("com.mikepenz:aboutlibraries-core:10.6.1")

                implementation(project(":network"))
                implementation(project(":datastore"))
                implementation(project(":model"))
                implementation(project(":database"))
            }
        }

        val androidMain by getting {
            apply(plugin = "kotlin-parcelize")
            dependencies {
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("androidx.core:core-ktx:1.9.0")
                implementation("androidx.activity:activity-ktx:1.6.1")
                implementation("androidx.activity:activity-compose:1.6.1")
                runtimeOnly("androidx.compose.material3:material3:1.0.1")
                implementation("androidx.multidex:multidex:2.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
                implementation("io.coil-kt:coil-compose:2.2.2")
                implementation("io.coil-kt:coil-svg:2.2.2")
                implementation("com.google.accompanist:accompanist-pager:$accompanist")
                implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist")
                implementation("com.google.accompanist:accompanist-flowlayout:$accompanist")
                implementation("androidx.constraintlayout:constraintlayout:2.1.4")
                implementation("com.google.android.material:material:1.8.0")
                implementation("androidx.core:core-splashscreen:1.0.0")

                implementation("androidx.media3:media3-exoplayer:$exoplayer")
                implementation("androidx.media3:media3-exoplayer-dash:$exoplayer")
                implementation("androidx.media3:media3-exoplayer-hls:$exoplayer")
                implementation("androidx.media3:media3-exoplayer-rtsp:$exoplayer")
                implementation("androidx.media3:media3-exoplayer-rtsp:$exoplayer")
                implementation("androidx.media3:media3-exoplayer-smoothstreaming:$exoplayer")
                implementation("androidx.media3:media3-ui:$exoplayer")

                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0-rc01")
            }
        }

        val desktopMain by getting {
            resources.srcDirs("src/desktopMain/resources", "src/commonMain/resources", "src/commonMain/assets")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.github.pdvrieze.xmlutil:core-jvm:0.85.0")
                implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.85.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines")
                implementation("com.sealwu:kscript-tools:1.0.22")
                implementation("net.harawata:appdirs:1.2.1")
                implementation("uk.co.caprica:vlcj:4.8.2")
                implementation("org.apache.commons:commons-lang3:3.12.0")

                val javaFxSuffix = getJavaFxSuffix()
                javafxModules.forEach { artifact ->
                    implementation(javaFxLib(artifact, javafx, javaFxSuffix))
                }
            }
        }
    }
}

android {
    sourceSets["main"].setRoot("src/androidMain/")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    sourceSets["main"].assets.srcDirs("src/androidMain/assets", "src/commonMain/assets")

    compileSdk = Configuration.compileSdk
    buildToolsVersion = Configuration.buildTools

    defaultConfig {
        applicationId = artifact
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = appCode
        versionName = appVersion

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
}

aboutLibraries {
    includePlatform = true
    duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
    duplicationRule = com.mikepenz.aboutlibraries.plugin.DuplicateRule.GROUP
    excludeFields = arrayOf("generated")
}

compose {
    kotlinCompilerPlugin.set(dependencies.compiler.forKotlin("1.7.20"))
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=1.8.10")

    desktop {
        application {
            mainClass = "dev.datlag.burningseries.MainKt"

            nativeDistributions {
                packageName = "Burning-Series"
                packageVersion = appVersion
                outputBaseDir.set(rootProject.buildDir.resolve("release"))
                description = "Watch any series from Burning-Series using this (unofficial) app."
                copyright = "© 2020 Jeff Retz (DatLag). All rights reserved."
                licenseFile.set(rootProject.file("LICENSE"))

                when (getHost()) {
                    Host.Linux -> targetFormats(
                        TargetFormat.AppImage, TargetFormat.Deb, TargetFormat.Rpm
                    )
                    Host.MAC -> targetFormats(
                        TargetFormat.Dmg
                    )
                    Host.Windows -> targetFormats(
                        TargetFormat.Exe, TargetFormat.Msi
                    )
                }


                linux {
                    iconFile.set(file("src/commonMain/assets/png/launcher_128.png"))
                    rpmLicenseType = "GPL-3.0"
                    debMaintainer = "Jeff Retz (DatLag)"
                    appCategory = "Video"
                }
                windows {
                    iconFile.set(file("src/commonMain/assets/ico/launcher_128.ico"))
                    upgradeUuid = "3487d337-1ef5-4e01-87cb-d1ede6e10752"
                }
                macOS {
                    iconFile.set(file("src/commonMain/assets/icns/launcher.icns"))
                }

                includeAllModules = true
            }
        }
    }
}

tasks.withType<Copy>().all {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

fun getJavaFxSuffix(): String {
    return when (osdetector.classifier) {
        "linux-x86_64" -> "linux"
        "linux-aarch_64" -> "linux-aarch64"
        "windows-x86_64" -> "win"
        "osx-x86_64" -> "mac"
        "osx-aarch_64" -> "mac-aarch64"
        else -> getHost().label
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

fun javaFxLib(artifactId: String, version: String, suffix: String): String {
    return "org.openjfx:${artifactId.replace('.', '-')}:${version}:${suffix}"
}

enum class Host(val label: String) {
    Linux("linux"),
    Windows("win"),
    MAC("mac");
}

apply(from = "fix-profm.gradle")
