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

    linuxX64()
    linuxArm64()

    mingwX64()

    macosX64()
    macosArm64()

    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.serialization)
            api(libs.immutable)
            implementation(libs.coroutines)
            implementation(libs.tooling)
        }
    }
}