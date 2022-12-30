package dev.datlag.burningseries.other

import androidx.compose.ui.res.useResource
import dev.datlag.burningseries.common.systemProperty
import java.io.InputStream

actual class Resources {
    actual fun getResourcesAsInputStream(location: String): InputStream? {
        val classLoader = Resources::class.java.classLoader ?: this::class.java.classLoader
        return runCatching {
            classLoader?.getResourceAsStream(location)
        }.getOrNull() ?: runCatching {
            Resources::class.java.getResourceAsStream(location)
        }.getOrNull() ?: runCatching {
            this::class.java.getResourceAsStream(location)
        }.getOrNull() ?: runCatching {
            useResource(location) { it }
        }.getOrNull()
    }

    actual companion object {
        actual val DEFAULT_STRINGS: String
            get() = "values/strings.xml"
        actual val GITHUB_ICON: String
            get() = "svg/GitHub.svg"

        actual val JAVASCRIPT_SCRAPE_HOSTER: String
            get() = "raw/scrape_hoster.js"

        actual val ABOUT_LIBRARIES: String
            get() = "aboutlibraries.json"

        actual val version: String?
            get() = systemProperty("jpackage.app-version")
    }

}