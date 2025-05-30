plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.serialization)
}

val artifact = "dev.datlag.mimasu.extension.provider"

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
            api(libs.tooling)
            api(libs.ktor)
            implementation(libs.serialization.protobuf)

            api(project(":matcher"))
            implementation(project("burningseries"))
            implementation(project("serienstream"))
        }
    }
}

android {
    compileSdk = 35
    namespace = artifact

    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_21
    }
}