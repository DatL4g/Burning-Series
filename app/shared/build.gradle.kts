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
}

val artifact = VersionCatalog.artifactName("shared")

group = artifact
version = appVersion

kotlin {
    androidTarget()
    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.animation)
                implementation(compose.animationGraphics)

                api(libs.stdlib)

                api(libs.decompose)
                api(libs.decompose.compose)
                implementation(libs.coroutines)
                api(libs.kodein)
                implementation(libs.kodein.compose)

                implementation(libs.aboutlibraries)
                implementation(libs.aboutlibraries.compose)

                api(libs.kamel)
                api(libs.napier)
                api(libs.moko.resources.compose)

                api(libs.windowsize.multiplatform)
                api(libs.insetsx)

                api(libs.ktor)
                api(libs.ktor.content.negotiation)
                api(libs.ktor.serialization.json)
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
                api(libs.ktor.android)
            }
        }
        val desktopMain by getting {
            dependsOn(commonMain)


        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
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