plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.multiplatform)
}

val artifact = VersionCatalog.artifactName()

kotlin {
    androidTarget()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":app:shared"))
            }
        }
    }
}

android {
    sourceSets["main"].setRoot("src/androidMain/")
    sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/resources")
    sourceSets["main"].assets.srcDirs("src/androidMain/assets", "src/commonMain/assets")

    compileSdk = Configuration.compileSdk
    namespace = artifact

    defaultConfig {
        applicationId = artifact
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = appVersionCode
        versionName = appVersion

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
    buildFeatures {
        buildConfig = true
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
