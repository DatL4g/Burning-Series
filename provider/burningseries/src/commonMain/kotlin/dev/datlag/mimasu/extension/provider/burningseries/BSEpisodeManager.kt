package dev.datlag.mimasu.extension.provider.burningseries

import co.touchlab.kermit.Logger
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
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

        val requestedEpisode = series.episodes.firstOrNull {
            it.number == episodeNumber
        } ?: return false

        return requestedEpisode.hoster.isNotEmpty() || run {
            val languageSpecificLinks = series.languages.map { lang ->
                show.toHref(newSeason = season, newLanguage = lang)
            }.toSet()

            coroutineScope {
                val languageSpecificSeries = languageSpecificLinks.map { langLink -> async {
                    getSeries(langLink)
                } }.awaitAll().filterNotNull().toSet()

                languageSpecificSeries.any { langSeries ->
                    val langEpisode = langSeries.episodes.firstOrNull {
                        it.number == episodeNumber
                    } ?: return@any false

                    langEpisode.hoster.isNotEmpty()
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
        Logger.e("Requested Series Href: $link")

        val series = getSeries(link) ?: return@coroutineScope run {
            Logger.e("No Series found for BS")
            emptyMap()
        }
        val allSeries = series.languages.map { lang ->
            show.toHref(newSeason = season, newLanguage = lang)
        }.toSet().map { langLink -> async {
            getSeries(langLink)
        } }.awaitAll().filterNotNull().toSet().distinctBy {
            it.selectedLanguage
        }.filterNot {
            it.selectedLanguage.isNullOrBlank()
        }

        Logger.e("Found Series: ${allSeries.size}")

        val mappedStreams = allSeries.map { s -> async {
            val requestedEpisode = s.episodes.firstOrNull { it.number == episodeNumber } ?: return@async run {
                Logger.e("No requested episode")
                null
            }
            val hosterUrls = firebaseWrapper?.store?.streams(requestedEpisode.hoster)?.ifEmpty {
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
            key to streams(httpClient, urls).toList()
        } }.awaitAll().toMap()

        return@coroutineScope mappedStreams
    }

    private suspend fun getSeries(link: String): Series? {
        return seriesKache.getIfAvailable(link) ?: (BurningSeries.series(httpClient, link) ?: fallbackClient?.let {
            BurningSeries.series(it, link)
        })?.also {
            seriesKache.put(link, it)
        }
    }

    private suspend fun streams(client: HttpClient, urls: Collection<String>) = coroutineScope {
        val directLinks = urls.map { url -> async {
            streamKache.getOrPut(url) {
                Skeo.loadVideos(client, url).map { it.url }
            }?.toSet()
        } }.awaitAll().filterNotNull().flatten().toSet()

        val reachableLinks = directLinks.map { link -> async {
            suspendCatching {
                val response = client.head(link)

                if (response.status.isSuccess()) {
                    link
                } else {
                    null
                }
            }.getOrNull()
        } }.awaitAll().filterNotNull()

        reachableLinks.map { it }.toSet()
    }

}