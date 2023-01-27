@file:Suppress("NewApi")

package dev.datlag.burningseries.other

import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import dev.datlag.burningseries.common.systemProperty
import java.io.File
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

    private fun loadFont(name: String, weight: FontWeight, italic: Boolean = false): Font {
        return Font(
            name,
            (getResourcesAsInputStream(name) ?: InputStream.nullInputStream()).readBytes(),
            weight,
            if (italic) FontStyle.Italic else FontStyle.Normal
        )
    }

    actual val Manrope by lazy(LazyThreadSafetyMode.NONE) {
        FontFamily(
            loadFont(MANROPE_EXTRA_LIGHT, FontWeight.ExtraLight),
            loadFont(MANROPE_EXTRA_LIGHT_ITALIC, FontWeight.ExtraLight, true),

            loadFont(MANROPE_LIGHT, FontWeight.Light),
            loadFont(MANROPE_LIGHT_ITALIC, FontWeight.Light, true),

            loadFont(MANROPE_REGULAR, FontWeight.Normal),
            loadFont(MANROPE_REGULAR_ITALIC, FontWeight.Normal, true),

            loadFont(MANROPE_MEDIUM, FontWeight.Medium),
            loadFont(MANROPE_MEDIUM_ITALIC, FontWeight.Medium, true),

            loadFont(MANROPE_SEMI_BOLD, FontWeight.SemiBold),
            loadFont(MANROPE_SEMI_BOLD_ITALIC, FontWeight.SemiBold, true),

            loadFont(MANROPE_BOLD, FontWeight.Bold),
            loadFont(MANROPE_BOLD_ITALIC, FontWeight.Bold, true),

            loadFont(MANROPE_EXTRA_BOLD, FontWeight.ExtraBold),
            loadFont(MANROPE_EXTRA_BOLD_ITALIC, FontWeight.ExtraBold, true),
        )
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

        private val FONT_FOLDER: String
            get() = "font/"

        private val MANROPE_EXTRA_LIGHT: String
            get() = "${FONT_FOLDER}manrope_extra_light.ttf"

        private val MANROPE_EXTRA_LIGHT_ITALIC: String
            get() = "${FONT_FOLDER}manrope_extra_light_italic.ttf"

        private val MANROPE_LIGHT: String
            get() = "${FONT_FOLDER}manrope_light.ttf"

        private val MANROPE_LIGHT_ITALIC: String
            get() = "${FONT_FOLDER}manrope_light_italic.ttf"

        private val MANROPE_REGULAR: String
            get() = "${FONT_FOLDER}manrope_regular.ttf"

        private val MANROPE_REGULAR_ITALIC: String
            get() = "${FONT_FOLDER}manrope_regular_italic.ttf"

        private val MANROPE_MEDIUM: String
            get() = "${FONT_FOLDER}manrope_medium.ttf"

        private val MANROPE_MEDIUM_ITALIC: String
            get() = "${FONT_FOLDER}manrope_medium_italic.ttf"

        private val MANROPE_SEMI_BOLD: String
            get() = "${FONT_FOLDER}manrope_semi_bold.ttf"

        private val MANROPE_SEMI_BOLD_ITALIC: String
            get() = "${FONT_FOLDER}manrope_semi_bold_italic.ttf"

        private val MANROPE_BOLD: String
            get() = "${FONT_FOLDER}manrope_bold.ttf"

        private val MANROPE_BOLD_ITALIC: String
            get() = "${FONT_FOLDER}manrope_bold_italic.ttf"

        private val MANROPE_EXTRA_BOLD: String
            get() = "${FONT_FOLDER}manrope_extra_bold.ttf"

        private val MANROPE_EXTRA_BOLD_ITALIC: String
            get() = "${FONT_FOLDER}manrope_extra_bold_italic.ttf"
    }

}