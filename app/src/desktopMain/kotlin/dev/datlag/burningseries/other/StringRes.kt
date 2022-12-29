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

    actual val allSeriesHeader: String
        get() = getLocaleOrDefaultFor("all_series_header")

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

    actual val back: String
        get() = getLocaleOrDefaultFor("back")

    actual val close: String
        get() = getLocaleOrDefaultFor("close")

    actual val clear: String
        get() = getLocaleOrDefaultFor("clear")

    actual val search: String
        get() = getLocaleOrDefaultFor("search")

    actual val confirm: String
        get() = getLocaleOrDefaultFor("confirm")

    actual val searchForSeries: String
        get() = getLocaleOrDefaultFor("search_for_series")

    actual val nextGenre: String
        get() = getLocaleOrDefaultFor("next_genre")

    actual val previousGenre: String
        get() = getLocaleOrDefaultFor("previous_genre")

    actual val rewind10: String
        get() = getLocaleOrDefaultFor("rewind")

    actual val play: String
        get() = getLocaleOrDefaultFor("play")

    actual val pause: String
        get() = getLocaleOrDefaultFor("pause")

    actual val loading: String
        get() = getLocaleOrDefaultFor("loading")

    actual val forward10: String
        get() = getLocaleOrDefaultFor("forward")

    actual val fullscreen: String
        get() = getLocaleOrDefaultFor("fullscreen")

    actual val selectLanguage: String
        get() = getLocaleOrDefaultFor("select_language")

    actual val noStreamingSourceHeader: String
        get() = getLocaleOrDefaultFor("no_streaming_source")

    actual val noStreamingSourceText: String
        get() = getLocaleOrDefaultFor("no_streaming_source_text")

    actual val activate: String
        get() = getLocaleOrDefaultFor("activate")

    actual val selectSeason: String
        get() = getLocaleOrDefaultFor("select_season")

    actual val saveStreamSuccess: String
        get() = getLocaleOrDefaultFor("save_stream_success")

    actual val saveStreamError: String
        get() = getLocaleOrDefaultFor("save_stream_error")

    actual val favorites: String
        get() = getLocaleOrDefaultFor("favorites")

    actual val lastWatched: String
        get() = getLocaleOrDefaultFor("last_watched")

    actual val latestEpisodes: String
        get() = getLocaleOrDefaultFor("latest_episodes")

    actual val latestSeries: String
        get() = getLocaleOrDefaultFor("latest_series")

    actual val linkedSeries: String
        get() = getLocaleOrDefaultFor("linked_series")

    actual val about: String
        get() = getLocaleOrDefaultFor("about")

    actual val beta: String
        get() = getLocaleOrDefaultFor("beta")

    actual val betaText: String
        get() = getLocaleOrDefaultFor("beta_text")

    actual val underConstruction: String
        get() = getLocaleOrDefaultFor("under_construction")

    actual val underConstructionText: String
        get() = getLocaleOrDefaultFor("under_construction_text")

    actual val enterUserName: String
        get() = getLocaleOrDefaultFor("enter_username")

    actual val enterPassword: String
        get() = getLocaleOrDefaultFor("enter_password")

    actual val skip: String
        get() = getLocaleOrDefaultFor("skip")

    actual val continueEpisode: String
        get() = getLocaleOrDefaultFor("continue_episode")

    actual val startEpisode: String
        get() = getLocaleOrDefaultFor("start_episode")

    actual val readMore: String
        get() = getLocaleOrDefaultFor("read_more")

    actual val sortHosterHint: String
        get() = getLocaleOrDefaultFor("sort_hoster_hint")

    actual val moveUp: String
        get() = getLocaleOrDefaultFor("move_up")

    actual val moveDown: String
        get() = getLocaleOrDefaultFor("move_down")

    actual val hosterOrder: String
        get() = getLocaleOrDefaultFor("hoster_order")

    actual val hosterOrderText: String
        get() = getLocaleOrDefaultFor("hoster_order_text")

    actual val noHoster: String
        get() = getLocaleOrDefaultFor("no_hoster")

    actual val noHosterText: String
        get() = getLocaleOrDefaultFor("no_hoster_text")

    actual val copyright: String
        get() = getLocaleOrDefaultFor("copyright")

    actual val vlcMustBeInstalled: String
        get() = getLocaleOrDefaultFor("vlc_must_be_installed")

    actual val mostPreferred: String
        get() = getLocaleOrDefaultFor("most_preferred")

    actual val leastPreferred: String
        get() = getLocaleOrDefaultFor("least_preferred")

    actual val tooManyRequests: String
        get() = getLocaleOrDefaultFor("too_many_requests")

    actual val errorTryAgain: String
        get() = getLocaleOrDefaultFor("error_try_again")

    actual val loadingHome: String
        get() = getLocaleOrDefaultFor("loading_home")

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