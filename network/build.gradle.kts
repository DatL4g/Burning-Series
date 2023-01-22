plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    id("de.jensklingenberg.ktorfit")
}

group = "dev.datlag.burningseries.network"

val flower = "3.0.0"
val ktorfit = "1.0.0-beta17"
val jsunpacker = "1.0.1"
val jsoup = "1.15.3"

dependencies {
    implementation("io.github.hadiyarajesh.flower-core:flower:$flower")
    implementation("dev.datlag.jsunpacker:jsunpacker:$jsunpacker")
    implementation("org.jsoup:jsoup:$jsoup")
    implementation(project(":datastore"))
    implementation(project(":model"))

    api("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfit")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfit")
}