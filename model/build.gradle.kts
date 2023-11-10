plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.android.library)
}

val artifact = VersionCatalog.artifactName("model")
group = artifact

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.parcelable)
                api(libs.serialization.json)
                api(libs.coroutines)
            }
        }

        val javaMain by creating {
            dependsOn(commonMain)

            jvmMain.get().dependsOn(this)
            androidMain.get().dependsOn(this)
        }

        jvmMain.get().dependencies {
            api(libs.lang)
        }
    }
}

android {
    compileSdk = Configuration.compileSdk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    namespace = artifact

    defaultConfig {
        minSdk = Configuration.minSdk
    }

    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
}