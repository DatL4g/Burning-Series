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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
            val burningSeries = async {
                matchedShowResults.burningSeries?.let {
                    series(
                        request = request,
                        searchItem = it.data
                    )
                }
            }
            val serienStream = async {
                matchedShowResults.serienStream?.let {
                    series(
                        request = request,
                        searchItem = it.data
                    )
                }
            }
            val streamkiste = async {
                matchedShowResults.streamkiste?.let {
                    series(
                        request = request,
                        item = it.data
                    )
                }
            }

            listOf(burningSeries, serienStream, streamkiste).awaitAll().any { it == true }
        }
    }

    suspend fun episodeStreams(
        matchedShowResults: MatchedShowResults,
        request: Show.EpisodeRequest
    ): Map<Show.Response.SourceInfo, List<String>> {
        return coroutineScope {
            val burningSeries = async { matchedShowResults.burningSeries?.let {
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
            } }

            val serienStream = async { matchedShowResults.serienStream?.let {
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
            } }

            val streamkiste = async { matchedShowResults.streamkiste?.let {
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
            } }

            buildMap {
                val bsBetterThanSS = when {
                    matchedShowResults.burningSeries != null && matchedShowResults.serienStream != null -> {
                        matchedShowResults.burningSeries.similarity > matchedShowResults.serienStream.similarity
                    }
                    matchedShowResults.burningSeries != null -> true
                    matchedShowResults.serienStream != null -> false
                    else -> false
                }
                val bsBetterThanSK = when {
                    matchedShowResults.burningSeries != null && matchedShowResults.streamkiste != null -> {
                        matchedShowResults.burningSeries.similarity > matchedShowResults.streamkiste.similarity
                    }
                    matchedShowResults.burningSeries != null -> true
                    matchedShowResults.streamkiste != null -> false
                    else -> false
                }
                val ssBetterThanSK = when {
                    matchedShowResults.serienStream != null && matchedShowResults.streamkiste != null -> {
                        matchedShowResults.serienStream.similarity > matchedShowResults.streamkiste.similarity
                    }
                    matchedShowResults.serienStream != null -> true
                    matchedShowResults.streamkiste != null -> false
                    else -> false
                }

                if (bsBetterThanSS) {
                    if (bsBetterThanSK) {
                        burningSeries.await()?.let(::putAll)

                        if (ssBetterThanSK) {
                            serienStream.await()?.let(::putAll)
                            streamkiste.await()?.let(::putAll)
                        } else {
                            streamkiste.await()?.let(::putAll)
                            serienStream.await()?.let(::putAll)
                        }
                    } else {
                        streamkiste.await()?.let(::putAll)
                        burningSeries.await()?.let(::putAll)
                        serienStream.await()?.let(::putAll)
                    }
                } else {
                    if (bsBetterThanSK) {
                        if (ssBetterThanSK) {
                            serienStream.await()?.let(::putAll)
                            burningSeries.await()?.let(::putAll)
                            streamkiste.await()?.let(::putAll)
                        } else {
                            burningSeries.await()?.let(::putAll)
                            streamkiste.await()?.let(::putAll)
                            serienStream.await()?.let(::putAll)
                        }
                    } else {
                        if (ssBetterThanSK) {
                            serienStream.await()?.let(::putAll)
                            streamkiste.await()?.let(::putAll)
                        } else {
                            streamkiste.await()?.let(::putAll)
                            serienStream.await()?.let(::putAll)
                        }
                        burningSeries.await()?.let(::putAll)
                    }
                }
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
}