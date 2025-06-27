package dev.datlag.mimasu.extension.provider.burningseries

import co.touchlab.kermit.Logger
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.burningseries.model.Series
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.http.isSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class BSEpisodeManager(
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient?,
    private val firebaseWrapper: FirebaseWrapper?
) {

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

    suspend fun episodeAvailable(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?
    ): Boolean {
        val link = show.toHref(newSeason = season, newLanguage = null)

        val series = getSeries(link) ?: return false
        val foundEpisode = findEpisode(series, episodeNumber) ?: return false

        return foundEpisode.target.hoster.isNotEmpty() || run {
            val languageSpecificLinks = foundEpisode.relatedSeries.languages.map { lang ->
                foundEpisode.relatedSeries.toHref(newLanguage = lang)
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
        season: Int?
    ): Map<String, List<String>> = coroutineScope {
        val link = show.toHref(newSeason = season, newLanguage = null)
        val series = getSeries(link) ?: return@coroutineScope run {
            Logger.e("No Series found for BS")
            emptyMap()
        }
        val foundEpisode = findEpisode(series, episodeNumber) ?: return@coroutineScope run {
            Logger.e("No Episode found for Series")
            emptyMap()
        }

        val allSeries = foundEpisode.relatedSeries.languages.map { lang ->
            foundEpisode.relatedSeries.toHref(newLanguage = lang)
        }.toSet().map { langLink -> async {
            getSeries(langLink)
        } }.awaitAll().filterNotNull().toSet().distinctBy {
            it.selectedLanguage
        }.filterNot {
            it.selectedLanguage.isNullOrBlank()
        }

        Logger.e("Found Series: ${allSeries.size}")

        val mappedStreams = allSeries.map { s -> async {
            val requestedEpisode = findEpisode(s, foundEpisode.episodeNumber, searchInNextSeason = false) ?: return@async run {
                Logger.e("No requested episode")
                null
            }
            val hosterUrls = firebaseWrapper?.store?.streams(requestedEpisode.target.hoster)?.ifEmpty {
                Logger.e("FirebaseWrapper gave empty stream collection")
                null
            } ?: return@async run {
                Logger.e("No Firebase Hoster URLs")
                null
            }

            (s.selectedLanguage ?: return@async run {
                Logger.e("No Selected Language")
                null
            }) to hosterUrls.ifEmpty { return@async run {
                Logger.e("No Associated Hoster URL for language")
                null
            } }
        } }.awaitAll().filterNotNull().toSet().map { (key, urls) -> async {
            key to streams(urls).toList()
        } }.awaitAll().toMap()

        return@coroutineScope mappedStreams
    }

    private suspend fun getSeries(link: String): Series? {
        return seriesKache.async(link) ?:  (BurningSeries.series(httpClient, link) ?: fallbackClient?.let {
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

}