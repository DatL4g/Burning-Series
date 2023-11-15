plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
}

val artifact = VersionCatalog.artifactName("sekret")
group = artifact

kotlin {
    androidTarget()
    androidNativeX86 {
        binaries {
            sharedLib()
        }
    }
    androidNativeX64 {
        binaries {
            sharedLib()
        }
    }
    androidNativeArm32 {
        binaries {
            sharedLib()
        }
    }
    androidNativeArm64 {
        binaries {
            sharedLib()
        }
    }

    jvm()
    iosX64 {
        binaries {
            sharedLib()
        }
    }
    iosArm64 {
        binaries {
            sharedLib()
        }
    }
    iosSimulatorArm64 {
        binaries {
            sharedLib()
        }
    }

    linuxX64 {
        binaries {
            sharedLib()
        }
    }
    linuxArm64 {
        binaries {
            sharedLib()
        }
    }
    macosX64 {
        binaries {
            sharedLib()
        }
    }
    macosArm64 {
        binaries {
            sharedLib()
        }
    }
    mingwX64 {
        binaries {
            sharedLib()
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("dev.datlag.sekret:sekret:0.1.0")
            }
        }
        val jniNativeMain by creating {
            nativeMain.orNull?.let { dependsOn(it) }
            androidNativeMain.orNull?.dependsOn(this)
            linuxMain.orNull?.dependsOn(this)
            mingwMain.orNull?.dependsOn(this)
        }
        val jniMain by creating {
            androidMain.orNull?.dependsOn(this)
            jvmMain.orNull?.dependsOn(this)
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