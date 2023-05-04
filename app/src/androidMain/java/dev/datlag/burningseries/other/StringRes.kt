package dev.datlag.burningseries.other

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dev.datlag.burningseries.R

actual class StringRes(private val context: Context) {

    actual val appName: String
        get() = context.getString(R.string.app_name)

    actual val allSeriesHeader: String
        get() = context.getString(R.string.all_series_header)

    actual val login: String
        get() = context.getString(R.string.login)

    actual val episodes: String
        get() = context.getString(R.string.episodes)

    actual val seriesSingular: String
        get() = context.getString(R.string.series_singular)

    actual val seriesPlural: String
        get() = context.getString(R.string.series_plural)

    actual val githubRepository: String
        get() = context.getString(R.string.github_repository)

    actual val more: String
        get() = context.getString(R.string.more)

    actual val settings: String
        get() = context.getString(R.string.settings)

    actual val showPassword: String
        get() = context.getString(R.string.show_password)

    actual val hidePassword: String
        get() = context.getString(R.string.hide_password)

    actual val back: String
        get() = context.getString(R.string.back)

    actual val close: String
        get() = context.getString(R.string.close)

    actual val clear: String
        get() = context.getString(R.string.clear)

    actual val search: String
        get() = context.getString(R.string.search)

    actual val confirm: String
        get() = context.getString(R.string.confirm)

    actual val searchForSeries: String
        get() = context.getString(R.string.search_for_series)

    actual val nextGenre: String
        get() = context.getString(R.string.next_genre)

    actual val previousGenre: String
        get() = context.getString(R.string.previous_genre)

    actual val rewind10: String
        get() = context.getString(R.string.rewind)

    actual val play: String
        get() = context.getString(R.string.play)

    actual val pause: String
        get() = context.getString(R.string.pause)

    actual val loading: String
        get() = context.getString(R.string.loading)

    actual val forward10: String
        get() = context.getString(R.string.forward)

    actual val fullscreen: String
        get() = context.getString(R.string.fullscreen)

    actual val selectLanguage: String
        get() = context.getString(R.string.select_language)

    actual val noStreamingSourceHeader: String
        get() = context.getString(R.string.no_streaming_source)

    actual val noStreamingSourceText: String
        get() = context.getString(R.string.no_streaming_source_text)

    actual val activate: String
        get() = context.getString(R.string.activate)

    actual val selectSeason: String
        get() = context.getString(R.string.select_season)

    actual val saveStreamSuccess: String
        get() = context.getString(R.string.save_stream_success)

    actual val saveStreamError: String
        get() = context.getString(R.string.save_stream_error)

    actual val favorites: String
        get() = context.getString(R.string.favorites)

    actual val lastWatched: String
        get() = context.getString(R.string.last_watched)

    actual val latestEpisodes: String
        get() = context.getString(R.string.latest_episodes)

    actual val latestSeries: String
        get() = context.getString(R.string.latest_series)

    actual val linkedSeries: String
        get() = context.getString(R.string.linked_series)

    actual val about: String
        get() = context.getString(R.string.about)

    actual val beta: String
        get() = context.getString(R.string.beta)

    actual val betaText: String
        get() = context.getString(R.string.beta_text)

    actual val underConstruction: String
        get() = context.getString(R.string.under_construction)

    actual val underConstructionText: String
        get() = context.getString(R.string.under_construction_text)

    actual val enterUserName: String
        get() = context.getString(R.string.enter_username)

    actual val enterPassword: String
        get() = context.getString(R.string.enter_password)

    actual val skip: String
        get() = context.getString(R.string.skip)

    actual val continueEpisode: String
        get() = context.getString(R.string.continue_episode)

    actual val startEpisode: String
        get() = context.getString(R.string.start_episode)

    actual val readMore: String
        get() = context.getString(R.string.read_more)

    actual val readLess: String
        get() = context.getString(R.string.read_less)

    actual val sortHosterHint: String
        get() = context.getString(R.string.sort_hoster_hint)

    actual val moveUp: String
        get() = context.getString(R.string.move_up)

    actual val moveDown: String
        get() = context.getString(R.string.move_down)

    actual val hosterOrder: String
        get() = context.getString(R.string.hoster_order)

    actual val hosterOrderText: String
        get() = context.getString(R.string.hoster_order_text)

    actual val noHoster: String
        get() = context.getString(R.string.no_hoster)

    actual val noHosterText: String
        get() = context.getString(R.string.no_hoster_text)

    actual val copyright: String
        get() = context.getString(R.string.copyright)

    actual val vlcMustBeInstalled: String
        get() = context.getString(R.string.vlc_must_be_installed)

    actual val mostPreferred: String
        get() = context.getString(R.string.most_preferred)

    actual val leastPreferred: String
        get() = context.getString(R.string.least_preferred)

    actual val tooManyRequests: String
        get() = context.getString(R.string.too_many_requests)

    actual val errorTryAgain: String
        get() = context.getString(R.string.error_try_again)

    actual val loadingHome: String
        get() = context.getString(R.string.loading_home)

    actual val newRelease: String
        get() = context.getString(R.string.new_release)

    actual val view: String
        get() = context.getString(R.string.view)

    actual val watch: String
        get() = context.getString(R.string.watch)

    actual val `continue`: String
        get() = context.getString(R.string.continue_text)

    actual val saveSuccessHeader: String
        get() = context.getString(R.string.save_success_header)

    actual val saveErrorHeader: String
        get() = context.getString(R.string.save_error_header)

    actual val saveSuccess: String
        get() = context.getString(R.string.save_success)

    actual val saveError: String
        get() = context.getString(R.string.save_error)

    actual val loadingUrl: String
        get() = context.getString(R.string.loading_url)

    actual val downloadNow: String
        get() = context.getString(R.string.download_now)

    actual val loadingAll: String
        get() = context.getString(R.string.loading_all)

    actual val activateText: String
        get() = context.getString(R.string.activate_text)

    actual val browser: String
        get() = context.getString(R.string.browser)

    actual val canon: String
        get() = context.getString(R.string.canon)

    actual val filler: String
        get() = context.getString(R.string.filler)

    actual val mixed: String
        get() = context.getString(R.string.mixed)

    actual val appearance: String
        get() = context.getString(R.string.appearance)

    actual val theming: String
        get() = context.getString(R.string.theming)

    actual val lightTheme: String
        get() = context.getString(R.string.light_theme)

    actual val darkTheme: String
        get() = context.getString(R.string.dark_theme)

    actual val followSystem: String
        get() = context.getString(R.string.follow_system)

    actual val amoledMode: String
        get() = context.getString(R.string.amoled_mode)

    actual val activateWindow: String
        get() = context.getString(R.string.activate_window)

    actual val activateWindowOpenedText: String
        get() = context.getString(R.string.activate_window_opened_text)

    actual val waitComponentInit: String
        get() = context.getString(R.string.wait_component_init)

    actual val logging: String
        get() = context.getString(R.string.logging)

    actual val error: String
        get() = context.getString(R.string.error)

    actual val errorText: String
        get() = context.getString(R.string.error_text)

    actual val none: String
        get() = context.getString(R.string.none)

    actual val home: String
        get() = context.getString(R.string.home)

    actual val series: String
        get() = context.getString(R.string.series)

    actual val streams: String
        get() = context.getString(R.string.streams)

    actual val loggingText: String
        get() = context.getString(R.string.logging_text)

    actual fun openInBrowser(url: String): Boolean {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        if (runCatching {
            ContextCompat.startActivity(context, browserIntent, null)
        }.isSuccess) {
            return true
        }

        val newIntent = browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            ContextCompat.startActivity(context, newIntent, null)
        }.isSuccess
    }

    actual fun copyToClipboard(value: String) {
        val manager = context.getSystemService(ClipboardManager::class.java)
        manager?.setPrimaryClip(ClipData.newPlainText(appName, value))
    }
}