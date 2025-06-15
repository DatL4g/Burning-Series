package dev.datlag.mimasu.extension.provider.burningseries

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import dev.datlag.mimasu.extension.ksoup.allByClass
import dev.datlag.mimasu.extension.ksoup.allByTag
import dev.datlag.mimasu.extension.ksoup.firstByClass
import dev.datlag.mimasu.extension.ksoup.firstByTag
import dev.datlag.mimasu.extension.ksoup.href
import dev.datlag.mimasu.extension.ksoup.parseGet
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.burningseries.model.Series
import dev.datlag.mimasu.extension.provider.burningseries.model.SeriesData
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.getValue
import kotlin.time.Duration.Companion.hours

data object BurningSeries {

    private const val PROTOCOL_HTTPS = "https://"
    private const val HOST = "bs.to"
    private const val SEARCH_PATH = "andere-serien"
    const val TITLE = "Burning Series"

    internal val currentYear by lazy {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
    }

    private var searchItemsCacheTime = 0L
    private var cachedSearchItems = setOf<SearchItem>()
        get() {
            if (searchItemsCacheTime <= 0L || Clock.System.now().minus(12.hours).epochSeconds > searchItemsCacheTime) {
                return emptySet()
            }
            return field
        }
        set(value) {
            if (value.isNotEmpty()) {
                field = value.also {
                    if (it.isNotEmpty()) {
                        searchItemsCacheTime = Clock.System.now().epochSeconds
                    }
                }
            }
        }

    private val searchMutex = Mutex()

    private fun createLink(href: String): String {
        return if (!href.matches("^\\w+?://.*".toRegex())) {
            if (!href.startsWith('/')) {
                "$PROTOCOL_HTTPS$HOST/$href"
            } else {
                "$PROTOCOL_HTTPS$HOST${"(?!:|/{2,})(/.*)".toRegex().find(href)?.value}"
            }
        } else {
            href
        }
    }

    internal fun normalize(href: String): String {
        val regex = "serie\\S+".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(href)?.value ?: href
    }

    internal fun fixSeriesHref(href: String): String {
        return SeriesData.fromHref(normalize(href)).toHref()
    }

    internal fun commonSeriesHref(href: String): String {
        return SeriesData.fromHref(normalize(href)).copy(
            season = null,
            language = null
        ).toHref(
            newSeason = null,
            newLanguage = null
        )
    }

    private suspend fun document(client: HttpClient, href: String): Document? = suspendCatching {
        Ksoup.parseGet(
            url = createLink(href),
            client = client
        )
    }.getOrNull()

    internal suspend fun search(client: HttpClient): Set<SearchItem> {
        cachedSearchItems.also {
            if (it.isNotEmpty()) {
                return it
            }
        }

        return atomicSearch(client)
    }

    internal suspend fun series(client: HttpClient, href: String): Series? {
        val doc = document(client, fixSeriesHref(href)) ?: return null

        val selectedLanguageValue = doc.firstByClass("series-language")?.selectFirst("option[selected]")?.value()?.ifBlank { null }?.trim()
        var selectedLanguage: String? = null
        val languageElements = doc.firstByClass("series-language")?.select("option").orEmpty()

        val languages = languageElements.mapNotNull {
            val value = it.value().ifBlank { null }?.trim()
            val selected = it.selectFirst("option[selected]")?.value()

            if (!selected.isNullOrBlank() || (!selectedLanguageValue.isNullOrBlank() && selectedLanguageValue == value)) {
                selectedLanguage = value
            }
            value
        }.toSet()

        if (selectedLanguage.isNullOrBlank()) {
            selectedLanguage = selectedLanguageValue
            if (selectedLanguage.isNullOrBlank()) {
                selectedLanguage = languages.firstOrNull()
            }
        }

        val episodeElements = doc.firstByClass("serie")?.firstByClass("episodes")?.allByTag("tr").orEmpty().ifEmpty { null } ?: return null
        val episodeInfoList = episodeElements.mapNotNull { element ->
            val episodeList = element.allByTag("td").flatMap { it.allByTag("a") }.map { data ->
                val text = data.text()
                val episodeHref = data.href()?.let(::normalize)

                text.trim() to episodeHref
            }

            val episodeHref = when {
                episodeList.isEmpty() -> return@mapNotNull null
                !episodeList[0].second.isNullOrBlank() -> episodeList[0].second
                episodeList.size > 1 && !episodeList[1].second.isNullOrBlank() -> episodeList[1].second
                else -> return@mapNotNull null
            } ?: return@mapNotNull null

            val episodeTitle = if (episodeList.size > 1) episodeList[1].first.trim() else ""
            val hoster = episodeList.map { it.second }.filterNot { it.isNullOrBlank() }.toMutableList().apply {
                remove(episodeHref)
                remove(episodeHref.trim())
            }.filterNot { it.isNullOrBlank() }.filterNotNull()

            Series.Episode(
                number = episodeList[0].first.trim().toIntOrNull(),
                title = episodeTitle,
                href = episodeHref,
                hoster = hoster
            )
        }

        return Series(
            href = doc.location()?.let(::fixSeriesHref) ?: fixSeriesHref(href),
            selectedLanguage = selectedLanguage?.ifBlank { null }?.trim(),
            languages = languages,
            episodes = episodeInfoList
        )
    }

    private suspend fun atomicSearch(client: HttpClient): Set<SearchItem> = searchMutex.withLock {
        cachedSearchItems.also {
            if (it.isNotEmpty()) {
                return it
            }
        }

        val doc = document(client, SEARCH_PATH) ?: return emptySet()

        return doc.getElementById("seriesContainer")?.allByClass("genre")?.map { element ->
            val genre = element.firstByTag("strong")?.text()?.ifBlank { null }?.trim()

            element.allByTag("li").mapNotNull { li ->
                val linkElement = li.firstByTag("a")
                val title = linkElement?.text()?.ifBlank { null }?.trim()
                val href = linkElement?.href()?.ifBlank { null }?.trim()?.let(::normalize)?.trim()

                if (!title.isNullOrBlank() && !href.isNullOrBlank()) {
                    SearchItem(
                        title = title,
                        href = href,
                        genre = genre
                    )
                } else {
                    null
                }
            }
        }?.flatten()?.toSet()?.also {
            cachedSearchItems = it
        } ?: emptySet()
    }

}