package dev.datlag.burningseries.other

import android.content.res.AssetManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.datlag.burningseries.BuildConfig
import dev.datlag.burningseries.R
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

    actual val Manrope by lazy(LazyThreadSafetyMode.NONE) {
        FontFamily(
            Font(R.font.manrope_extra_light, FontWeight.ExtraLight),
            Font(R.font.manrope_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),

            Font(R.font.manrope_light, FontWeight.Light),
            Font(R.font.manrope_light_italic, FontWeight.Light, FontStyle.Italic),

            Font(R.font.manrope_regular, FontWeight.Normal),
            Font(R.font.manrope_regular_italic, FontWeight.Normal, FontStyle.Italic),

            Font(R.font.manrope_medium, FontWeight.Medium),
            Font(R.font.manrope_medium_italic, FontWeight.Medium, FontStyle.Italic),

            Font(R.font.manrope_semi_bold, FontWeight.SemiBold),
            Font(R.font.manrope_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),

            Font(R.font.manrope_bold, FontWeight.Bold),
            Font(R.font.manrope_bold_italic, FontWeight.Bold, FontStyle.Italic),

            Font(R.font.manrope_extra_bold, FontWeight.ExtraBold),
            Font(R.font.manrope_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic)
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
            get() = "raw/aboutlibraries.json"

        actual val version: String?
            get() = BuildConfig.VERSION_NAME
    }

}