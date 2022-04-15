import com.google.protobuf.gradle.protoc

plugins {
    id("idea")
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
    id("com.google.protobuf")
    id("io.michaelrocks.paranoid")
    id("com.klaxit.hiddensecrets")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    compileSdk = Configuration.compileSdk
    buildToolsVersion = Configuration.buildTools

    defaultConfig {
        applicationId = "de.datlag.burningseries"
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk
        versionCode = 200
        versionName = "2.0.0"

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = true
        }

        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
    kotlinOptions {
        jvmTarget = CompileOptions.jvmTarget
    }
    kapt {
        correctErrorTypes = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(mapOf("path" to ":network")))
    implementation(project(mapOf("path" to ":model")))
    implementation(project(mapOf("path" to ":database")))
    implementation(project(mapOf("path" to ":executor")))
    implementation(project(mapOf("path" to ":datastore")))
    implementation("org.jetbrains.kotlin:kotlin-stdlib${CompileOptions.kotlinJdk}:1.6.20")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    runtimeOnly("androidx.vectordrawable:vectordrawable-animated:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")

    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.github.devendroid:ReadMoreOption:1.0.3")
    implementation("com.mikepenz:aboutlibraries:10.1.0")

    implementation("com.google.android.exoplayer:exoplayer:2.17.1")
    implementation("com.github.DATL4G.PreviewSeekBar:previewseekbar-exoplayer:3.0.1")
    implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.16")
    implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native:1.0.16")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation("com.github.kirich1409", "viewbindingpropertydelegate").version {
        strictly("1.5.6")
    }

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.activity:activity-ktx:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.room:room-ktx:2.4.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    implementation("androidx.navigation:navigation-runtime-ktx:2.4.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")

    implementation("com.google.dagger:hilt-android:2.41")
    kapt("com.google.dagger:hilt-android-compiler:2.41")
    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("io.github.inflationx:viewpump:2.0.3")
    implementation("io.github.inflationx:calligraphy3:3.1.1")

    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    implementation("com.github.bumptech.glide", "glide").version {
        strictly("4.13.1")
    }
    kapt("com.github.bumptech.glide", "compiler").version {
        strictly("4.13.1")
    }
    implementation("com.github.DatL4g:Coilifier-Android:1.2.2") {
        exclude("com.github.bumptech.glide", "glide")
        exclude("com.github.bumptech.glide", "compiler")
    }

    implementation("androidx.datastore:datastore:1.0.0")

    implementation("com.github.DATL4G:SelectionBottomSheet:535724d01e")

    implementation("com.github.DatL4g:StatusBarAlert:2.0.1")
    implementation("com.github.Ferfalk:SimpleSearchView:0.2.0")

    implementation("com.github.SamuelGjk:RoundCornerProgressBar:1.0")
    implementation("com.github.DatL4g.Klient2Klient:Klient2Klient:0.1.1")
    implementation("net.openid:appauth:0.11.1")

    implementation("com.github.DATL4G:Android-Animated-Theme-Manager:release-SNAPSHOT")
}

protobuf.protobuf.run {
    protoc {
        artifact = "com.google.protobuf:protoc:3.20.0"
    }
}
