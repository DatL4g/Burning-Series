plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

val artifact = "dev.datlag.mimasu.extension.provider.animebase"

kotlin {
    jvmToolchain(21)
    jvm()

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }

    wasmJs {
        browser()
        nodejs()
        binaries.executable()
    }

    linuxX64()
    linuxArm64()

    mingwX64()

    androidNativeX64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines)
            api(libs.serialization.json)
            api(libs.ktorfit)
            api(libs.ktor)
            api(libs.ktor.content.negotiation)
            api(libs.ktor.serialization.json)
            api(libs.tooling)
            implementation(libs.ksoup)
        }
    }
}

android {
    compileSdk = 36
    namespace = artifact

    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_21
    }
}