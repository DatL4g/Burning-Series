package dev.datlag.burningseries.other

import android.content.res.AssetManager
import dev.datlag.burningseries.BuildConfig
import java.io.InputStream

actual class Resources(private val assetManager: AssetManager) {
    actual fun getResourcesAsInputStream(location: String): InputStream? {
        val classLoader = Resources::class.java.classLoader ?: this::class.java.classLoader
        return runCatching {
            classLoader?.getResourceAsStream(location)
        }.getOrNull() ?: runCatching {
            Resources::class.java.getResourceAsStream(location)
        }.getOrNull() ?: runCatching {
            this::class.java.getResourceAsStream(location)
        }.getOrNull() ?: runCatching {
            assetManager.open(location)
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
            get() = "raw/aboutlibraries.json"

        actual val version: String?
            get() = BuildConfig.VERSION_NAME
    }

}