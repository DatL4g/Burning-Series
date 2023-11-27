plugins {
  alias(libs.plugins.multiplatform)
}
kotlin {
  js(IR)
  applyDefaultHierarchyTemplate()
  sourceSets {
    val commonMain by getting {
      dependencies {
        api("dev.datlag.sekret:sekret:0.2.0")
      }
    }
  }
}
