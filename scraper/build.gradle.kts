plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dev.datlag.burningseries.scraper"

val jsoup = "1.16.1"
val coroutines = "1.6.4"
val ktor = "2.3.0"
val jsunpacker = "1.0.1"

dependencies {
    implementation(project(":model"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("io.ktor:ktor-client-core-jvm:$ktor")
    api("org.jsoup:jsoup:$jsoup")
    implementation("dev.datlag.jsunpacker:jsunpacker:$jsunpacker")
}
