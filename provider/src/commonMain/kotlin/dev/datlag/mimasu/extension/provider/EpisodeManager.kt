package dev.datlag.mimasu.extension.provider

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.kache.CachePool
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.provider.burningseries.BSEpisodeManager
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.MatchedShowResults
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.CombinedEpisodeManager
import dev.datlag.mimasu.extension.provider.streamkiste.Streamkiste
import dev.datlag.mimasu.extension.provider.streamkiste.StreamkisteEpisodeManager
import dev.datlag.mimasu.extension.provider.streamkiste.createStreamkiste
import dev.datlag.mimasu.extension.provider.streamkiste.model.Browse
import dev.datlag.mimasu.extension.provider.serienstream.model.LanguageInfo as SerienStreamLang
import dev.datlag.skeo.Skeo
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.scopeCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.hours
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem as SerienStreamItem
import dev.datlag.mimasu.extension.provider.burningseries.model.LanguageInfo as BSLang

/**
 * Resolve Episode and Streams of Series
 *
 * @param httpClient Default HttpClient, used for requesting everything
 * @param fallbackClient Fallback Client, used for requesting streams
 * @param dohClient DoH HttpClient, used for requesting websites
 *
 * Streams are not requested with DoH!
 */
class EpisodeManager(
    val httpClient: HttpClient,
    val fallbackClient: HttpClient,
    val dohClient: HttpClient?,
    val firebaseWrapper: FirebaseWrapper?
) : CachePool {

    private val burningSeriesEpisodeManager = BSEpisodeManager(
        httpClient = httpClient,
        fallbackClient = fallbackClient,
        dohClient = dohClient,
        firebaseWrapper = firebaseWrapper
    )

    private val serienStreamEpisodeManager = CombinedEpisodeManager(
        httpClient = httpClient,
        fallbackClient = fallbackClient,
        dohClient = dohClient
    )

    private val streamkiste = ktorfit {
        baseUrl(Streamkiste.BASE_URL)
        httpClient(httpClient)
    }.createStreamkiste()

    private val fallbackStreamkiste = dohClient?.let {
        ktorfit {
            baseUrl(Streamkiste.BASE_URL)
            httpClient(it)
        }.createStreamkiste()
    }

    private val streamkisteEpisodeManager = StreamkisteEpisodeManager(
        streamkiste = streamkiste,
        fallbackStreamkiste = fallbackStreamkiste,
        httpClient = httpClient,
        fallbackClient = fallbackClient
    )

    private val episodeKache = InMemoryKache<EpisodeKey, Boolean>(
        maxSize = 5L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
    }

    override suspend fun clear(): Boolean {
        return suspendCatching {
            episodeKache.clear()
        }.isSuccess
                && burningSeriesEpisodeManager.clear()
                && serienStreamEpisodeManager.clear()
                && streamkisteEpisodeManager.clear()
    }

    suspend fun episodeAvailability(
        showId: Int,
        matchedShowResults: MatchedShowResults,
        request: Show.EpisodeRequest
    ): Boolean {
        val episodeKey = EpisodeKey(
            showId = showId,
            season = request.season ?: return false,
            episode = request.episodeNumber ?: return false
        )

        episodeKache.async(episodeKey)?.let {
            return it
        }

        return coroutineScope {
            val deferreds = mutableListOf(
                async {
                    matchedShowResults.burningSeries?.let {
                        series(
                            request = request,
                            searchItem = it.data
                        )
                    } ?: false
                },
                async {
                    matchedShowResults.serienStream?.let {
                        series(
                            request = request,
                            searchItem = it.data
                        )
                    } ?: false
                },
                async {
                    matchedShowResults.streamkiste?.let {
                        series(
                            request = request,
                            item = it.data
                        )
                    } ?: false
                }
            )

            while (deferreds.isNotEmpty()) {
                val result = select {
                    deferreds.forEach { deferred ->
                        deferred.onAwait { value ->
                            deferreds.remove(deferred)
                            value
                        }
                    }
                }

                if (result) {
                    return@coroutineScope true
                }
            }

            return@coroutineScope false
        }
    }

    suspend fun episodeStreams(
        matchedShowResults: MatchedShowResults,
        request: Show.EpisodeRequest
    ): Map<Show.Response.SourceInfo, List<String>> {
        val gatheredResults = coroutineScope {
            val results = mutableListOf<ProviderStreamResult>()
            val deferreds = mutableListOf<Deferred<ProviderStreamResult>>(
                async {
                    val similarity = matchedShowResults.burningSeries?.similarity ?: -1.0
                    val streams = matchedShowResults.burningSeries?.let {
                        streams(
                            request = request,
                            searchItem = it.data
                        ).mapNotNull { (key, value) ->
                            key to Skeo.filterNotSample(value).ifEmpty {
                                return@mapNotNull null
                            }.toList()
                        }.associate { (k, v) ->
                            Show.Response.SourceInfo(
                                sourceTitle = BurningSeries.TITLE,
                                sourceLocale = k.localeTitle,
                                locale = k.locale
                            ) to v
                        }
                    }

                    ProviderStreamResult(similarity, streams)
                },
                async {
                    val similarity = matchedShowResults.serienStream?.similarity ?: -1.0
                    val streams = matchedShowResults.serienStream?.let {
                        streams(
                            request = request,
                            searchItem = it.data
                        ).mapNotNull { (key, value) ->
                            key to Skeo.filterNotSample(value).ifEmpty {
                                return@mapNotNull null
                            }.toList()
                        }.associate { (k, v) ->
                            Show.Response.SourceInfo(
                                sourceTitle = it.data.sourceTitle,
                                sourceLocale = k.localeTitle,
                                locale = k.locale
                            ) to v
                        }
                    }

                    ProviderStreamResult(similarity, streams)
                },
                async {
                    val similarity = matchedShowResults.streamkiste?.similarity ?: -1.0
                    val streams = matchedShowResults.streamkiste?.let {
                        mapOf(
                            Show.Response.SourceInfo(
                                sourceTitle = "StreamKiste",
                                sourceLocale = "German",
                                locale = "de"
                            ) to streams(
                                request = request,
                                item = it.data
                            )
                        )
                    }

                    ProviderStreamResult(similarity, streams)
                }
            )

            repeat(2) {
                val providerResult = select {
                    deferreds.forEach { deferred ->
                        deferred.onAwait { value ->
                            deferreds.remove(deferred)
                            value
                        }
                    }
                }

                results.add(providerResult)
            }

            val lastDeferred = deferreds.singleOrNull()
            val bothWereEmpty = results.all { it.streams.isNullOrEmpty() }

            if (bothWereEmpty) {
                lastDeferred?.await()?.let {
                    results.add(it)
                }
            } else {
                val lastResult = withTimeoutOrNull(3000) {
                    lastDeferred?.await()
                }

                if (lastResult != null) {
                    results.add(lastResult)
                } else {
                    lastDeferred?.cancelAndJoin()
                }
            }

            results
        }

        return buildMap {
            gatheredResults
                .sortedByDescending { it.similarity }
                .forEach { result ->
                    putAll(result.streams.orEmpty())
                }
        }
    }

    private suspend fun series(
        request: Show.EpisodeRequest,
        searchItem: SearchItem
    ): Boolean = burningSeriesEpisodeManager.episodeAvailable(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season,
        appLanguage = request.appLanguage
    )

    private suspend fun series(
        request: Show.EpisodeRequest,
        searchItem: SerienStreamItem
    ): Boolean = serienStreamEpisodeManager.episodeAvailable(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season
    )

    private suspend fun series(
        request: Show.EpisodeRequest,
        item: Browse.Item
    ): Boolean = streamkisteEpisodeManager.episodeAvailable(
        show = item,
        episodeNumber = request.episodeNumber,
        season = request.season
    )

    private suspend fun streams(
        request: Show.EpisodeRequest,
        searchItem: SearchItem
    ): Map<BSLang, List<String>> = burningSeriesEpisodeManager.episodeStreams(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season,
        appLanguage = request.appLanguage
    )

    private suspend fun streams(
        request: Show.EpisodeRequest,
        searchItem: SerienStreamItem
    ): Map<SerienStreamLang, List<String>> = serienStreamEpisodeManager.episodeStreams(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season,
        appLanguage = request.appLanguage
    )

    private suspend fun streams(
        request: Show.EpisodeRequest,
        item: Browse.Item
    ): List<String> = streamkisteEpisodeManager.episodeStreams(
        show = item,
        episodeNumber = request.episodeNumber,
        season = request.season,
    )

    data class EpisodeKey(
        val showId: Int,
        val season: Int,
        val episode: Int
    )

    private data class ProviderStreamResult(
        val similarity: Double,
        val streams: Map<Show.Response.SourceInfo, List<String>>?
    )
}