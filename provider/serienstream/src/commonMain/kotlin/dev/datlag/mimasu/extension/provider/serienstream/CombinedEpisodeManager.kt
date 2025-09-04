package dev.datlag.mimasu.extension.provider.serienstream

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.ksoup.parseGet
import dev.datlag.mimasu.extension.provider.serienstream.model.LanguageInfo
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem
import dev.datlag.mimasu.extension.provider.serienstream.model.Series
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.setFrom
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CombinedEpisodeManager(
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient?
) {

    private val seriesKache = InMemoryKache<String, Series>(
        maxSize = 5L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
    }

    private val episodeKache = InMemoryKache<String, EpisodeInfo>(
        maxSize = 5L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
    }

    private val streamKache = InMemoryKache<String, Collection<String>>(
        maxSize = 2L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 10.minutes
    }

    suspend fun episodeAvailable(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?
    ): Boolean {
        val link = show.toSlug(newSeason = season)
        val series = getSeries(link, show) ?: return false
        val foundEpisode = findEpisode(show, series, episodeNumber) ?: return false
        val episodeInfo = episodeKache.async(foundEpisode.target.slug)
            ?: episodeDocument(show, foundEpisode.target)?.let {
                EpisodeInfo(
                    languages = foundEpisode.target.availableLanguages(it),
                    provider = foundEpisode.target.providers(it)
                )
            }?.also { e ->
                episodeKache.async(foundEpisode.target.slug) { e }
            } ?: return false

        return episodeInfo.provider.isNotEmpty()
    }

    suspend fun episodeStreams(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?,
        appLanguage: String?
    ): Map<LanguageInfo, List<String>> = coroutineScope {
        val link = show.toSlug(newSeason = season)
        val series = getSeries(link, show) ?: return@coroutineScope emptyMap()
        val foundEpisode = findEpisode(show, series, episodeNumber) ?: return@coroutineScope emptyMap()
        val episodeInfo = episodeKache.async(foundEpisode.target.slug)
            ?: episodeDocument(show, foundEpisode.target)?.let {
                EpisodeInfo(
                    languages = foundEpisode.target.availableLanguages(it),
                    provider = foundEpisode.target.providers(it)
                )
            }?.also { e ->
                episodeKache.async(foundEpisode.target.slug) { e }
            } ?: return@coroutineScope emptyMap()

        val mappedHoster = episodeInfo.provider.groupBy { it.languageKey }.mapNotNull { (key, provider) ->
            val links = provider.map { show.createLink(it.redirectSlug) }.ifEmpty { null } ?: return@mapNotNull null
            key to links.toSet()
        }.toSet().map { (key, urls) ->
            val title = episodeInfo.languages.firstOrNull {
                it.key == key
            }?.title ?: key.toString()
            val lang = LanguageInfo(
                localeTitle = title,
                locale = show.localeCodes[key] ?: title
            )

            lang to urls
        }

        val (preferred, other) = mappedHoster.partition { (lang, _) ->
            !appLanguage.isNullOrBlank()
                    && (lang.locale.equals(appLanguage, ignoreCase = true)
                    || lang.locale.startsWith(appLanguage, ignoreCase = true)
                    || appLanguage.startsWith(lang.locale, ignoreCase = true))
        }

        val preferredStreams = preferred.map { (lang, urls) -> async {
            val streams = streams(urls).ifEmpty { null } ?: return@async null
            lang to streams.toList()
        } }.awaitAll().filterNotNull().toSet()

        val otherStreams = other.map { (lang, urls) -> async {
            val streams = streams(urls).ifEmpty { null } ?: return@async null
            lang to streams.toList()
        } }.awaitAll().filterNotNull().toSet()

        return@coroutineScope setFrom(preferredStreams, otherStreams).toMap()
    }

    private suspend fun getSeries(link: String, item: SearchItem): Series? {
        return seriesKache.async(link) ?: (Series.from(item, link, httpClient) ?: fallbackClient?.let {
            Series.from(item, link, it)
        })?.also { s ->
            seriesKache.async(link) { s }
        }
    }

    private suspend fun findEpisode(
        searchItem: SearchItem,
        series: Series,
        episodeNumber: Int?,
        searchInNextSeason: Boolean = true
    ): FoundEpisode? {
        val found = series.episodes.firstOrNull {
            it.number == episodeNumber
        } ?: episodeNumber?.let { series.episodes.elementAtOrNull(it - 1) }

        if (found != null) {
            return FoundEpisode(
                relatedSeries = series,
                target = found,
                episodeNumber = episodeNumber
            )
        }

        if (!searchInNextSeason) {
            return null
        }

        val nextSeriesEpisodeNumber = episodeNumber?.let {
            val subtracted = it - series.episodes.size
            if (subtracted > 0) {
                subtracted
            } else {
                null
            }
        } ?: return null

        val nextSeason = series.nextSeason ?: return null
        val nextSeries = getSeries(searchItem.toSlug(newSeason = nextSeason), searchItem)?.takeIf {
            it.season == nextSeason
        } ?: return null

        return findEpisode(searchItem, nextSeries, nextSeriesEpisodeNumber, searchInNextSeason)
    }

    private suspend fun episodeDocument(
        show: SearchItem,
        episode: Series.Episode
    ): Document? {
        return suspendCatching {
            Ksoup.parseGet(show.createLink(show.normalize(episode.slug)), httpClient)
        }.getOrNull() ?: fallbackClient?.let {
            suspendCatching {
                Ksoup.parseGet(show.createLink(show.normalize(episode.slug)), it)
            }.getOrNull()
        }
    }

    private suspend fun streams(urls: Collection<String>) = coroutineScope {
        val directLinks = urls.map { url -> async {
            streamKache.async(url) {
                Skeo.resolveStreams(url, httpClient)
            }?.toSet()
        } }.awaitAll().filterNotNull().flatten().toSet()
        val items = Skeo.filterNotSample(directLinks)

        val reachableLinks = Skeo.filterReachable(items, httpClient)

        reachableLinks.map { it }.toSet()
    }

    data class FoundEpisode(
        val relatedSeries: Series,
        val target: Series.Episode,
        val episodeNumber: Int?
    )

    data class EpisodeInfo(
        val languages: Collection<Series.Episode.Language>,
        val provider: Collection<Series.Episode.Provider>
    )
}