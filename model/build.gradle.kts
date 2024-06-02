plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
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
    }
}