plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit")
}

group = "dev.datlag.burningseries.network"

val flower = "3.0.0"
val ktorfit = "1.0.0"
val jsunpacker = "1.0.1"
val jsoup = "1.15.4"
val coroutines = "1.6.4"

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.hadiyarajesh.flower-core:flower:$flower")
                implementation(project(":model"))

                api("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfit")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("dev.datlag.jsunpacker:jsunpacker:$jsunpacker")
                implementation("org.jsoup:jsoup:$jsoup")
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
    add("kspJvm", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
    add("kspJs", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
}
