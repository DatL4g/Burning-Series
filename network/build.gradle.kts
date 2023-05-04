plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit")
    id("com.android.library")
}

group = "dev.datlag.burningseries.network"

val flower = "3.0.0"
val ktorfit = "1.1.0"
val coroutines = "1.6.4"

kotlin {
    android()
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.hadiyarajesh.flower-core:flower:$flower")
                implementation(project(":model"))

                api("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfit")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(project(":scraper"))
            }
        }

        val androidMain by getting {
            dependencies {
                api(project(":scraper"))
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
    add("kspJvm", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
    add("kspAndroid", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
    add("kspJs", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
}

android {
    sourceSets["main"].setRoot("src/androidMain/")

    compileSdk = Configuration.compileSdk
    buildToolsVersion = Configuration.buildTools

    defaultConfig {
        minSdk = Configuration.minSdk
        namespace = "dev.datlag.burningseries.network"
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
}
