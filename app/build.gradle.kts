import org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.android.application")
    id("kotlin-parcelize") apply false
}

val coroutines = "1.6.4"
val decompose = "1.0.0-beta-01"
val kodein = "7.16.0"
val ktor = "2.1.3"

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
    android()
    jvm("desktop")

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("11"))
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)

                api("com.arkivanov.decompose:decompose:$decompose")
                api("com.arkivanov.decompose:extensions-compose-jetbrains:$decompose")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
                api("org.kodein.di:kodein-di:$kodein")
                implementation("org.kodein.di:kodein-di-framework-compose:$kodein")
                api("io.ktor:ktor-client-okhttp:$ktor")
                api("io.ktor:ktor-client-content-negotiation:$ktor")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor")
                api("com.squareup.okhttp3:okhttp-dnsoverhttps:4.10.0")

                implementation(project(":network"))
                implementation(project(":datastore"))
                implementation(project(":model"))
            }
        }

        val androidMain by getting {
            apply(plugin = "kotlin-parcelize")
            dependencies {
                implementation("androidx.appcompat:appcompat:1.5.1")
                implementation("androidx.core:core-ktx:1.9.0")
                implementation("androidx.activity:activity-ktx:1.6.1")
                implementation("androidx.activity:activity-compose:1.6.1")
                runtimeOnly("androidx.compose.material3:material3:1.0.1")
                implementation("androidx.multidex:multidex:2.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
                implementation("io.coil-kt:coil-compose:2.2.2")
                implementation("io.coil-kt:coil-svg:2.2.2")
            }
        }

        val desktopMain by getting {
            resources.srcDirs("src/desktopMain/resources", "src/commonMain/resources", "src/commonMain/assets")
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.github.pdvrieze.xmlutil:core-jvm:0.84.3")
                implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.84.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines")
                implementation("com.sealwu:kscript-tools:1.0.21")
                implementation("net.harawata:appdirs:1.2.1")
            }
        }
    }
}

android {
    sourceSets["main"].setRoot("src/androidMain/")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    sourceSets["main"].assets.srcDirs("src/androidMain/assets", "src/commonMain/assets")

    compileSdk = Configuration.compileSdk
    buildToolsVersion = Configuration.buildTools

    defaultConfig {
        applicationId = "dev.datlag.burningseries"
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = 400
        versionName = "4.0.0"

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
}

compose.desktop {
    application {
        mainClass = "dev.datlag.burningseries.MainKt"
    }
}

tasks.withType<Copy>().all {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
