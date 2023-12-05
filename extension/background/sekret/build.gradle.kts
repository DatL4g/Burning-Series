plugins {
    alias(libs.plugins.multiplatform)
}
kotlin {
    js(IR) {
      binaries.executable()
      browser()
      nodejs()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.sekret)
            }
        }
    }
}
