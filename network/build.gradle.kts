plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktorfit) apply false
    alias(libs.plugins.realm)
}

val artifact = VersionCatalog.artifactName("network")

group = artifact

kotlin {
    jvm()
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
        nodejs()
        binaries.executable()
    }

    jvmToolchain(CompileOptions.jvmTargetVersion)

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":model"))

                api(libs.ktorfit)
                api(libs.serialization.json)
                api(libs.coroutines)
                api(libs.flowredux)
                api(libs.ktsoup)
                api(libs.ktsoup.fs)
                api(libs.ktsoup.ktor)
                implementation(libs.jsunpacker)
                api(libs.firebase.auth)
                api(libs.firebase.store)
            }
        }

        val realmCompatible by creating {
            dependsOn(commonMain)
            dependencies {
                api(libs.realm)
                api(libs.realm.sync)
            }
        }

        val androidMain by getting {
            dependsOn(realmCompatible)
            dependencies {
                api(libs.firebase.android)
                api(libs.firebase.android.auth)
                api(libs.firebase.android.firestore)
            }
        }

        jvmMain.get().dependsOn(realmCompatible)
        nativeMain.get().dependsOn(realmCompatible)
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
    packaging {
        resources.merges.add("META-INF/LICENSE")
        resources.merges.add("META-INF/DEPENDENCIES")
        resources.pickFirsts.add("**")
        resources.pickFirsts.add("**/*")
        resources.pickFirsts.add("*")
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.ktorfit.ksp)
    add("kspAndroid", libs.ktorfit.ksp)
    add("kspJvm", libs.ktorfit.ksp)
    add("kspIosX64", libs.ktorfit.ksp)
    add("kspIosArm64", libs.ktorfit.ksp)
    add("kspIosSimulatorArm64", libs.ktorfit.ksp)
    add("kspJs", libs.ktorfit.ksp)
}