plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
}

val artifact = "dev.datlag.mimasu.extension.provider.burningseries"

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
            api(libs.serialization.json)
            api(libs.ktorfit)
            api(libs.ktor)
            api(libs.ktor.content.negotiation)
            api(libs.ktor.serialization.json)
            api(libs.tooling)
            implementation(libs.datetime)
            implementation(libs.kermit)
            implementation(libs.skeo)

            implementation(project(":matcher"))
            implementation(project(":kache"))
            implementation(project(":ksoup"))
            implementation(project(":firebase"))
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