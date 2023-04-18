plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dev.datlag.burningseries.scraper"

val jsoup = "1.15.4"
val coroutines = "1.6.4"
val ktor = "2.2.3"

dependencies {
    implementation(project(":model"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("io.ktor:ktor-client-core-jvm:$ktor")
    implementation("org.jsoup:jsoup:$jsoup")
}
