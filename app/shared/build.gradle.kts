import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.aboutlibraries)
    id("kotlin-parcelize") apply false
    alias(libs.plugins.serialization)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.sekret)
}

val artifact = VersionCatalog.artifactName("shared")

group = artifact
version = appVersion

sekret {
    packageName = artifact
    propertiesFile = rootDir.canonicalPath
}

kotlin {
    androidTarget()
    jvm("desktop")

    jvmToolchain(CompileOptions.jvmTargetVersion)

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.ui)
                api(compose.animation)
                api(compose.animationGraphics)

                api(libs.stdlib)

                api(libs.decompose)
                api(libs.decompose.compose)
                api(libs.coroutines)
                api(libs.kodein)
                api(libs.kodein.compose)

                implementation(libs.aboutlibraries)
                implementation(libs.aboutlibraries.compose)

                api(libs.kamel)
                api(libs.moko.resources.compose)
                api(libs.blurhash)

                api(libs.windowsize.multiplatform)
                api(libs.insetsx)

                api(libs.ktor)
                api(libs.ktor.content.negotiation)
                api(libs.ktor.serialization.json)

                api(project(":model"))
                api(project(":network"))
                api(project(":color"))
                api(project(":database"))
                api(project("sekret"))
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
            apply(plugin = "kotlin-parcelize")

            dependencies {
                api(libs.activity)
                api(libs.activity.compose)
                api(libs.android)
                api(libs.appcompat)
                api(libs.coroutines.android)
                api(libs.material)
                api(libs.multidex)
                api(libs.splashscreen)
                api(libs.ktor.jvm)
                api(libs.media3)
                api(libs.media3.dash)
                api(libs.media3.hls)
                api(libs.media3.rtsp)
                api(libs.media3.session)
                api(libs.media3.smooth)
                api(libs.media3.ui)
                api(libs.media3.cast)
                api(libs.cast)
                api(libs.cast.framework)
                api(libs.accompanist.uicontroller)
                api(libs.webview.android)
                api(libs.permission)
                api(libs.nanoid)
            }
        }
        val desktopMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(compose.desktop.currentOs)
                api(libs.coroutines.swing)
                api(libs.context.menu)
                api(libs.window.styler)
                api(libs.ktor.jvm)
                api(libs.appdirs)
                api(libs.vlcj)
                api(libs.webview.desktop)
            }
        }
    }
}

android {
    sourceSets["main"].setRoot("src/androidMain/")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    sourceSets["main"].assets.srcDirs("src/androidMain/assets", "src/commonMain/assets")

    compileSdk = Configuration.compileSdk
    namespace = artifact

    defaultConfig {
        minSdk = Configuration.minSdk
    }
    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources.merges.add("META-INF/LICENSE")
        resources.merges.add("META-INF/DEPENDENCIES")
        resources.pickFirsts.add("**")
        resources.pickFirsts.add("**/*")
        resources.pickFirsts.add("*")
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}

multiplatformResources {
    multiplatformResourcesPackage = artifact
    multiplatformResourcesClassName = "SharedRes"
}

aboutLibraries {
    includePlatform = true
    duplicationMode = DuplicateMode.MERGE
    duplicationRule = DuplicateRule.GROUP
    excludeFields = arrayOf("generated")
}

val createNativeLib = tasks.create("createNativeLib") {
    dependsOn(tasks.generateSekret, "sekret:assemble")

    fun getBinPath(target: String): String? {
        val buildDir = project("sekret").layout.buildDirectory.asFile.get()
        return if (File(buildDir, "bin/$target/releaseShared").exists()) {
            File(buildDir, "bin/$target/releaseShared").canonicalPath
        } else if (File(buildDir, "bin/$target/debugShared").exists()) {
            File(buildDir, "bin/$target/debugShared").canonicalPath
        } else {
            null
        }
    }

    doLast {
        val androidArm32 = getBinPath("androidNativeArm32")
        val androidArm64 = getBinPath("androidNativeArm64")
        val androidX64 = getBinPath("androidNativeX64")
        val androidX86 = getBinPath("androidNativeX86")

        if (androidArm32 != null) {
            copy {
                from(androidArm32) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../android/src/androidMain/jniLibs/armeabi-v7a")
            }
        }
        if (androidArm64 != null) {
            copy {
                from(androidArm64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../android/src/androidMain/jniLibs/arm64-v8a")
            }
        }
        if (androidX64 != null) {
            copy {
                from(androidX64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../android/src/androidMain/jniLibs/x86_64")
            }
        }
        if (androidX86 != null) {
            copy {
                from(androidX86) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../android/src/androidMain/jniLibs/x86")
            }
        }

        val linuxArm64 = getBinPath("linuxArm64")
        val linuxX64 = getBinPath("linuxX64")
        val mingwX64 = getBinPath("mingwX64")
        val macosArm64 = getBinPath("macosArm64")
        val macosX64 = getBinPath("macosX64")

        if (linuxArm64 != null) {
            copy {
                from(linuxArm64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../desktop/resources/linux-arm64")
            }
        }
        if (linuxX64 != null) {
            copy {
                from(linuxX64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../desktop/resources/linux-x64")
            }
        }
        if (mingwX64 != null) {
            copy {
                from(mingwX64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../desktop/resources/windows")
            }
        }
        if (macosArm64 != null) {
            copy {
                from(macosArm64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../desktop/resources/macos-arm64")
            }
        }
        if (macosX64 != null) {
            copy {
                from(macosX64) {
                    exclude("*.h")
                    exclude("*.def")
                }
                into("../desktop/resources/macos-x64")
            }
        }
    }
}