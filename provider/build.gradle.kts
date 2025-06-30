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

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines)
            api(libs.tooling)
            api(libs.ktor)
            implementation(libs.serialization.protobuf)
            implementation(libs.kermit)
            implementation(libs.skeo)

            implementation(project(":kache"))
            api(project(":matcher"))
            api(project(":firebase"))
            api(project("burningseries"))
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