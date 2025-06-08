package dev.datlag.mimasu.extension.provider.burningseries

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

    suspend fun episodeAvailable(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?
    ): Boolean {
        val link = if (show.href.endsWith('/')) {
            "${show.href}${season}"
        } else {
            "${show.href}/${season}"
        }

        val series = getSeries(link) ?: return false

        val requestedEpisode = series.episodes.firstOrNull {
            it.number == episodeNumber
        } ?: return false

        return requestedEpisode.hoster.isNotEmpty() || run {
            val languageSpecificLinks = series.languages.map { lang ->
                if (link.endsWith('/')) {
                    "${link}$lang"
                } else {
                    "${link}/$lang"
                }
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
        val link = if (show.href.endsWith('/')) {
            "${show.href}${season}"
        } else {
            "${show.href}/${season}"
        }

        val series = getSeries(link) ?: return@coroutineScope emptyMap()
        val allSeries = series.languages.map { lang ->
            if (link.endsWith('/')) {
                "${link}$lang"
            } else {
                "${link}/$lang"
            }
        }.toSet().map { langLink -> async {
            getSeries(langLink)
        } }.awaitAll().filterNotNull().toSet()

        val mappedStreams = allSeries.map { s -> async {
            val requestedEpisode = s.episodes.firstOrNull { it.number == episodeNumber } ?: return@async null
            val hosterUrls = firebaseWrapper?.store?.streams(requestedEpisode.hoster)?.ifEmpty { null } ?: return@async null

            (s.selectedLanguage ?: return@async null) to streams(httpClient, hosterUrls).toList()
        } }.awaitAll().filterNotNull().toMap()

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
            Skeo.loadVideos(client, url)
        } }.awaitAll().flatten().toSet()

        val reachableLinks = directLinks.map { link -> async {
            suspendCatching {
                val response = client.head(link.url)

                if (response.status.isSuccess()) {
                    link
                } else {
                    null
                }
            }.getOrNull()
        } }.awaitAll().filterNotNull()

        reachableLinks.map { it.url }.toSet()
    }

}