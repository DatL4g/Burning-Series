plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            api(libs.immutable)
            implementation(libs.coroutines)
            implementation(libs.ktor)
            implementation(libs.ktor.network)
            implementation(libs.ktor.network.tls)
            implementation(libs.serialization.json)
            implementation(libs.tooling)
        }
    }
}