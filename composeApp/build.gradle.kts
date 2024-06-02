import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.konfig)
    alias(libs.plugins.serialization)
}

val artifact = "dev.datlag.burningseries"
val appVersion = "6.0.0"
val appVersionCode = 600

group = artifact
version = appVersion

composeCompiler {
    enableStrongSkippingMode.set(true)
    enableNonSkippingGroupOptimization.set(true)
}

buildkonfig {
    packageName = artifact

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "packageName", artifact)
    }
}

kotlin {
    androidTarget()
    jvm()

    /*listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }*/

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.kodein)
            implementation(libs.kodein.compose)

            implementation(libs.haze)
            implementation(libs.haze.materials)

            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            implementation(libs.tooling.decompose)

            implementation(libs.windowsize)
        }

        val androidMain by getting {
            apply(plugin = "kotlin-parcelize")

            dependencies {
                implementation(libs.android)
                implementation(libs.activity)
                implementation(libs.activity.compose)
                implementation(libs.multidex)
                implementation(libs.androidx.window)

                implementation(libs.ktor.jvm)
                implementation(libs.coroutines.android)
            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.coroutines.swing)
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}

android {
    sourceSets["main"].setRoot("src/androidMain/")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    sourceSets["main"].assets.srcDirs("src/androidMain/assets", "src/commonMain/assets")
    compileSdk = 34
    namespace = artifact

    defaultConfig {
        applicationId = artifact
        minSdk = 23
        targetSdk = 43
        versionCode = appVersionCode
        versionName = appVersion

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "values**"
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

compose {
    desktop {
        application {
            mainClass = "$artifact.MainKt"
        }
    }
    web { }
}
