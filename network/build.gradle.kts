plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

group = "dev.datlag.burningseries.network"

val flower = "3.0.0"
val ktorfit = "1.0.0-beta16"

dependencies {
    implementation("io.github.hadiyarajesh.flower-core:flower:$flower")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    api("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfit")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
}