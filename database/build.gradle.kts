plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

val artifact = VersionCatalog.artifactName("database")

group = artifact

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(CompileOptions.jvmTargetVersion)

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.sqldelight.coroutines)
                api(project(":model"))
            }
        }

        androidMain.get().dependencies {
            implementation(libs.sqldelight.android)
            api(libs.android.sqlite.framework)
        }

        jvmMain.get().dependencies {
            implementation(libs.sqldelight.jvm)
        }

        iosMain.get().dependencies {
            implementation(libs.sqldelight.native)
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

sqldelight {
    databases {
        create("BurningSeries") {
            packageName.set(artifact)
            srcDirs("src/commonMain/bs")
        }
    }
}