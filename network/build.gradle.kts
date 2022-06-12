plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("io.michaelrocks.paranoid")
    id("com.apollographql.apollo3") version "3.3.0"
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
    implementation(project(mapOf("path" to ":model")))
    implementation(project(mapOf("path" to ":database")))
    implementation(project(mapOf("path" to ":datastore")))

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    
    api("io.github.hadiyarajesh:flower:2.0.0")
    api("com.squareup.retrofit2:retrofit:2.9.0") {
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okhttp3", "logging-interceptor")
    }
    api("com.squareup.okhttp3", "okhttp").version {
        strictly("4.9.3")
    }
    api("com.squareup.okhttp3", "logging-interceptor").version {
        strictly("4.9.3")
    }
    implementation("javax.inject:javax.inject:1")
    implementation("org.jsoup:jsoup:1.15.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.2")
    api("com.kttdevelopment:mal4j:2.8.1")
    api("com.apollographql.apollo3:apollo-runtime:3.3.0")
}

apollo {
    service("anilist") {
        srcDir("src/main/graphql/anilist")
        packageName.set("de.datlag.network.anilist")

        introspection {
            endpointUrl.set("https://graphql.anilist.co")
            schemaFile.set(file("src/main/graphql/anilist/schema.graphqls"))
        }
    }
    service("github") {
        srcDir("src/main/graphql/github")
        packageName.set("de.datlag.network.github")

        introspection {
            headers.set(mapOf("Authorization" to "Bearer token"))
            endpointUrl.set("https://api.github.com/graphql")
            schemaFile.set(file("src/main/graphql/github/schema.graphqls"))
        }
    }
}