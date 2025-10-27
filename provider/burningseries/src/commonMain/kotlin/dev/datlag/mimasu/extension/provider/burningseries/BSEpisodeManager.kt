package dev.datlag.mimasu.extension.provider.burningseries

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.kache.CachePool
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.provider.burningseries.model.LanguageInfo
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.burningseries.model.Series
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.setFrom
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Resolve Episode and Streams of Series
 *
 * @param httpClient Default HttpClient, used for requesting everything
 * @param fallbackClient Fallback Client, used for requesting streams
 * @param dohClient DoH HttpClient, used for requesting websites
 *
 * Streams are not requested with DoH!
 */
class BSEpisodeManager(
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient,
    private val dohClient: HttpClient?,
    private val firebaseWrapper: FirebaseWrapper?
) : CachePool {

    private val seriesKache = InMemoryKache<String, Series>(
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

    override suspend fun clear(): Boolean {
        return suspendCatching {
            streamKache.clear()
        }.isSuccess && suspendCatching {
            seriesKache.clear()
        }.isSuccess
    }

    suspend fun episodeAvailable(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?,
        appLanguage: String?
    ): Boolean {
        val link = show.toHref(newSeason = season, newLanguage = appLanguage)

        val series = getSeries(link)?.takeIf {
            season == null || it.season == season
        } ?: return false
        val foundEpisode = findEpisode(series, episodeNumber) ?: return false

        return foundEpisode.target.hoster.isNotEmpty() || run {
            val languageSpecificLinks = foundEpisode.relatedSeries.languages.map { lang ->
                foundEpisode.relatedSeries.toHref(newLanguage = lang.locale)
            }.toSet()

            coroutineScope {
                val languageSpecificSeries = languageSpecificLinks.map { langLink -> async {
                    getSeries(langLink)
                } }.awaitAll().filterNotNull().toSet()

                languageSpecificSeries.any { langSeries ->
                    val langEpisode = findEpisode(
                        series = langSeries,
                        episodeNumber = foundEpisode.episodeNumber,
                        searchInNextSeason = false
                    ) ?: return@any false

                    langEpisode.target.hoster.isNotEmpty()
                }
            }
        }
    }

    suspend fun episodeStreams(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?,
        appLanguage: String?
    ): Map<LanguageInfo, List<String>> = coroutineScope {
        val link = show.toHref(newSeason = season, newLanguage = appLanguage)
        val series = getSeries(link)?.takeIf {
            season == null || it.season == season
        } ?: return@coroutineScope emptyMap()
        val foundEpisode = findEpisode(series, episodeNumber) ?: return@coroutineScope emptyMap()

        val allSeries = foundEpisode.relatedSeries.languages.map { lang ->
            foundEpisode.relatedSeries.toHref(newLanguage = lang.locale)
        }.toSet().map { langLink -> async {
            getSeries(langLink)
        } }.awaitAll().filterNotNull().toSet().distinctBy {
            it.selectedLanguage
        }.filterNot {
            it.selectedLanguage.isNullOrBlank()
        }
        val allLanguages = allSeries.flatMap { it.languages }.toSet()

        val mappedHoster = allSeries.map { s -> async {
            val requestedEpisode = findEpisode(s, foundEpisode.episodeNumber, searchInNextSeason = false) ?: return@async null
            val hosterUrls = firebaseWrapper?.store?.streams(requestedEpisode.target.hoster)?.ifEmpty { null } ?: return@async null

            (s.selectedLanguage ?: return@async null) to hosterUrls.ifEmpty { return@async null }
        } }.awaitAll().filterNotNull().toSet()

        val (preferred, other) = mappedHoster.partition { (lang, _) ->
            !appLanguage.isNullOrBlank()
                    && (lang.equals(appLanguage, ignoreCase = true)
                    || lang.startsWith(appLanguage, ignoreCase = true)
                    || appLanguage.startsWith(lang, ignoreCase = true))
        }

        val preferredStreams = preferred.map { (key, urls) -> async {
            val lang = allLanguages.firstOrNull {
                it.locale == key
            } ?: allLanguages.firstOrNull {
                it.localeTitle == key
            } ?: LanguageInfo(
                localeTitle = key,
                locale = key
            )

            lang to streams(urls).toList()
        } }.awaitAll().toSet()

        val otherStreams = other.map { (key, urls) -> async {
            val lang = allLanguages.firstOrNull {
                it.locale == key
            } ?: allLanguages.firstOrNull {
                it.localeTitle == key
            } ?: LanguageInfo(
                localeTitle = key,
                locale = key
            )

            lang to streams(urls).toList()
        } }.awaitAll().toSet()

        return@coroutineScope setFrom(preferredStreams, otherStreams).toMap()
    }

    private suspend fun getSeries(link: String): Series? {
        return seriesKache.async(link) ?:  (BurningSeries.series(httpClient, link) ?: dohClient?.let {
            BurningSeries.series(it, link)
        })?.also { s ->
            seriesKache.async(link) { s }
        }
    }

    private suspend fun findEpisode(
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
        val nextSeason = (series.nextSeason ?: series.season?.takeUnless {
            it <= 0
        }?.plus(1)) ?: return null
        val nextSeries = getSeries(series.toHref(newSeason = nextSeason))?.takeIf {
            it.season == nextSeason
        } ?: return null

        return findEpisode(nextSeries, nextSeriesEpisodeNumber, searchInNextSeason)
    }

    private suspend fun streams(urls: Collection<String>) = coroutineScope {
        val directLinks = urls.map { url -> async {
            streamKache.async(url) {
                suspendCatching {
                    Skeo.resolveStreams(url, httpClient)
                }.getOrNull() ?: suspendCatching {
                    Skeo.resolveStreams(url, fallbackClient)
                }.getOrNull()
            }?.toSet()
        } }.awaitAll().filterNotNull().flatten().toSet()
        val items = Skeo.filterNotSample(directLinks)

        val reachableLinks = Skeo.filterReachable(items, httpClient)

        reachableLinks.toSet()
    }

    data class FoundEpisode(
        val relatedSeries: Series,
        val target: Series.Episode,
        val episodeNumber: Int?
    )

}