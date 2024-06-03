plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    id("kotlinx-atomicfu")
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.serialization)
            implementation(libs.immutable)
            implementation(libs.coroutines)
        }
    }
}