plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.atomicfu)
}

kotlin {
    androidTarget()
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.tooling)
            implementation(libs.coroutines)
            implementation(libs.kache)
            implementation(libs.kermit)

            api(libs.firebase)
            api(libs.firebase.firestore)
        }

        androidMain.dependencies {
            api(project.dependencies.platform(libs.android.firebase))
        }
    }
}

android {
    compileSdk = 35
    namespace = "dev.datlag.mimasu.extension.firebase"

    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_21
    }
}