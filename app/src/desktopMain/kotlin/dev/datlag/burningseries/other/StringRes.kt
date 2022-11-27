@file:Suppress("NewApi")

package dev.datlag.burningseries.other

import dev.datlag.burningseries.common.openInBrowser
import dev.datlag.burningseries.model.XMLResources
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.util.Locale

actual class StringRes private constructor(
    private val defaultStringRes: XMLResources,
    private val localeStringRes: XMLResources?
) {
    actual val appName: String
        get() = getLocaleOrDefaultFor("app_name")

    actual val login: String
        get() = getLocaleOrDefaultFor("login")

    actual val episodes: String
        get() = getLocaleOrDefaultFor("episodes")

    actual val seriesSingular: String
        get() = getLocaleOrDefaultFor("series_singular")

    actual val seriesPlural: String
        get() = getLocaleOrDefaultFor("series_plural")

    actual val githubRepository: String
        get() = getLocaleOrDefaultFor("github_repository")

    actual val more: String
        get() = getLocaleOrDefaultFor("more")

    actual val settings: String
        get() = getLocaleOrDefaultFor("settings")

    actual val showPassword: String
        get() = getLocaleOrDefaultFor("show_password")

    actual val hidePassword: String
        get() = getLocaleOrDefaultFor("hide_password")

    private fun getLocaleOrDefaultFor(name: String): String {
        val defaultValue = defaultStringRes.strings.firstOrNull { it.name == name }
            ?: defaultStringRes.strings.firstOrNull { it.name.equals(name, true) }

        if (defaultValue?.translatable == false) {
            return defaultValue.data
        }

        val localeValue = localeStringRes?.strings?.firstOrNull { it.name == name }
            ?: localeStringRes?.strings?.firstOrNull { it.name.equals(name, true) }

        return localeValue?.data ?: defaultValue?.data ?: String()
    }

    actual fun openInBrowser(url: String): Boolean {
        return url.openInBrowser("Unsupported System").isSuccess
    }

    companion object {

        fun create(resources: Resources): StringRes {
            val format = XML {
                recommended()
            }
            val defaultStringRes = runCatching {
                String(resources.getResourcesAsInputStream(Resources.DEFAULT_STRINGS)!!.readAllBytes())
            }.getOrNull() ?: throw IllegalStateException("No string resources defined or found")

            val localeStringRes = runCatching {
                format.decodeFromString<XMLResources>(String(resources.getResourcesAsInputStream("values-${Locale.getDefault().language}/strings.xml")!!.readAllBytes()))
            }.getOrNull()

            val xml = runCatching {
                format.decodeFromString<XMLResources>(defaultStringRes)
            }.getOrNull() ?: throw IllegalStateException("Could not parse default string resources")
            return StringRes(xml, localeStringRes)
        }
    }
}