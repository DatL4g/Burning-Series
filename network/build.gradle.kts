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
        commonMain.dependencies {
            api(libs.coroutines)
            implementation(libs.datetime)
            api(libs.napier)
            implementation(libs.ksoup)
            implementation(libs.ktor)
            implementation(libs.tooling)
            implementation(libs.immutable)
            api(libs.flowredux)
            api(libs.skeo)

            api(project(":model"))
            implementation(project(":firebase"))
        }
    }
}