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
    alias(libs.plugins.sekret)
    alias(libs.plugins.serialization)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
}

val artifact = "dev.datlag.mimasu.extension"
val appVersion = "10.0.0"
val appVersionCode = 1000

group = artifact
version = appVersion

kotlin {
    jvmToolchain(21)
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.tooling.compose)
            implementation(libs.kodein)
            implementation(libs.kodein.compose)
            implementation(libs.semver)
            implementation(libs.kermit)
            implementation(libs.lifecycle)
            implementation(libs.navigation)
            implementation(libs.viewmodel)
            implementation(libs.reflection)

            implementation(project(":github"))
            implementation(project(":provider"))
            implementation(project(":firebase"))
            implementation(project(":kache"))
        }

        androidMain.dependencies {
            implementation(libs.android)
            implementation(libs.activity)
            implementation(libs.activity.compose)
            implementation(libs.multidex)
            implementation(libs.android.startup)
            implementation(libs.google.fonts)

            implementation(libs.ktor.jvm)
            implementation(libs.coroutines.android)
            implementation(libs.okhttp.doh)
            implementation(libs.service)
            implementation(libs.serialization.protobuf)
            implementation(libs.webview)
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
}

sekret {
    properties {
        enabled.set(true)

        googleServicesFile.set(project.layout.projectDirectory.file("google-services.json"))
    }
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
    signingConfigs {
        maybeCreate("release").apply {
            storeFile = rootProject.layout.projectDirectory.file("keystore.jks").asFile
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("src/androidMain/proguard-rules.pro")
            )
        }
    }
}

compose {
    resources {
        generateResClass = auto
    }
}