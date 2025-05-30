import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

val artifact = "dev.datlag.mimasu.extension"
val appVersion = "10.0.0"
val appVersionCode = 1000

group = artifact
version = appVersion

kotlin {
    jvmToolchain(21)
    androidTarget()

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
            implementation(libs.tooling)
            implementation(libs.kodein)

            implementation(project(":github"))
            implementation(project(":provider"))
        }

        val androidMain by getting {
            apply(plugin = "kotlin-parcelize")
            apply(plugin = libs.plugins.crashlytics.get().pluginId)

            dependencies {
                implementation(libs.android)
                implementation(libs.activity)
                implementation(libs.activity.compose)
                implementation(libs.multidex)

                implementation(libs.ktor.jvm)
                implementation(libs.coroutines.android)
                implementation(libs.okhttp.doh)
                implementation("androidx.lifecycle:lifecycle-service:2.9.0")
                implementation(libs.serialization.protobuf)

                implementation("org.htmlunit:htmlunit3-android:4.3.0")
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}

android {
    sourceSets["main"].setRoot("src/androidMain/")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    sourceSets["main"].aidl.srcDirs("src/androidMain/aidl")
    compileSdk = 35
    namespace = artifact

    defaultConfig {
        applicationId = artifact
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersion

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
        aidl = true
    }
}
