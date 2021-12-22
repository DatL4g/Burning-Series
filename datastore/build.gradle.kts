import com.google.protobuf.gradle.builtins
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("com.google.protobuf")
    id("io.michaelrocks.paranoid")
}

android {
    compileSdk = Configuration.compileSdk
    buildToolsVersion = Configuration.buildTools

    defaultConfig {
        minSdk = Configuration.minSdk
        targetSdk = Configuration.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = CompileOptions.sourceCompatibility
        targetCompatibility = CompileOptions.targetCompatibility
    }
    kotlinOptions {
        jvmTarget = CompileOptions.jvmTarget
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    api("androidx.datastore:datastore-core:1.0.0")
    api("com.google.protobuf:protobuf-javalite:3.19.1")
}

protobuf.protobuf.run {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
