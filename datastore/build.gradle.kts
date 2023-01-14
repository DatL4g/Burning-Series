plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.protobuf")
}

group = "dev.datlag.burningseries.datastore"

kotlin {
    android()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("androidx.datastore:datastore-core:1.0.0")
                api(project(":datastore-codegen"))
            }
        }

        val androidMain by getting {
            dependencies {
                api("androidx.datastore:datastore:1.0.0")
            }
        }

        val desktopMain by getting {

        }
    }
}

android {
    sourceSets["main"].setRoot("src/androidMain/")

    compileSdk = Configuration.compileSdk
    buildToolsVersion = Configuration.buildTools

    defaultConfig {
        minSdk = Configuration.minSdk
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
}
