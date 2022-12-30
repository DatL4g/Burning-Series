package dev.datlag.burningseries.other

import java.io.InputStream

expect class Resources {

    fun getResourcesAsInputStream(location: String): InputStream?

    companion object {
        val DEFAULT_STRINGS: String
        val GITHUB_ICON: String
        val JAVASCRIPT_SCRAPE_HOSTER: String
        val ABOUT_LIBRARIES: String

        val version: String?
    }

}

