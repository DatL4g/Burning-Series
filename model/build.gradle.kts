plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id ("kotlin-parcelize") apply false
}

group = "dev.datlag.burningseries.model"

kotlin {
    android()
    jvm("desktop")

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("11"))
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.arkivanov.essenty:parcelable:0.7.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            }
        }

        val androidMain by getting {
            apply(plugin = "kotlin-parcelize")
        }

        val desktopMain by getting
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
