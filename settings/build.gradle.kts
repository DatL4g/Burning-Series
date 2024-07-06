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
            api(libs.datastore)
            api(libs.immutable)

            implementation(libs.serialization.protobuf)
            implementation(libs.tooling)
            implementation(libs.datetime)
            implementation(libs.oidc.tokenstore)
        }
    }
}
