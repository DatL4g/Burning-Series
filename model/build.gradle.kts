plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.android.library)
    id ("kotlin-parcelize") apply false
}

val artifact = VersionCatalog.artifactName("model")
group = artifact

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }

    jvmToolchain(CompileOptions.jvmTargetVersion)

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.parcelable)
                api(libs.serialization.json)
                api(libs.coroutines)
                api(libs.datetime)
                api(libs.napier)
            }
        }

        val javaMain by creating {
            dependsOn(commonMain)

            jvmMain.get().dependsOn(this)
            androidMain.get().dependsOn(this)
        }

        androidMain.get().apply {
            apply(plugin = "kotlin-parcelize")
        }

        jvmMain.get().dependencies {
            api(libs.lang)
        }

        jsMain.get().dependencies {
            api(libs.coroutines.js)
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
    packaging {
        resources.merges.add("META-INF/LICENSE")
        resources.merges.add("META-INF/DEPENDENCIES")
        resources.pickFirsts.add("**")
        resources.pickFirsts.add("**/*")
        resources.pickFirsts.add("*")
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}