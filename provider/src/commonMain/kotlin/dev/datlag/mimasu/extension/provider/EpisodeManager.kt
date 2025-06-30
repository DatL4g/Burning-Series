package dev.datlag.mimasu.extension.provider

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.kache.async
import dev.datlag.mimasu.extension.provider.burningseries.BSEpisodeManager
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.MatchedShowResults
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.CombinedEpisodeManager
import dev.datlag.mimasu.extension.provider.serienstream.model.LanguageInfo as SerienStreamLang
import dev.datlag.skeo.Skeo
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Duration.Companion.hours
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem as SerienStreamItem
import dev.datlag.mimasu.extension.provider.burningseries.model.LanguageInfo as BSLang

class EpisodeManager(
    val httpClient: HttpClient,
    val fallbackClient: HttpClient?,
    val firebaseWrapper: FirebaseWrapper?
) {

    private val burningSeriesEpisodeManager = BSEpisodeManager(
        httpClient = httpClient,
        fallbackClient = fallbackClient,
        firebaseWrapper = firebaseWrapper
    )

    private val serienStreamEpisodeManager = CombinedEpisodeManager(
        httpClient = httpClient,
        fallbackClient = fallbackClient
    )

    private val episodeKache = InMemoryKache<EpisodeKey, Boolean>(
        maxSize = 5L * 1024 * 1024
    ) {
        strategy = KacheStrategy.LRU
        expireAfterWriteDuration = 12.hours
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

            listOf(burningSeries, serienStream).awaitAll().any { it == true }
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

            buildMap {
                if (matchedShowResults.burningSeries != null && matchedShowResults.serienStream != null) {
                    if (matchedShowResults.burningSeries.similarity > matchedShowResults.serienStream.similarity) {
                        burningSeries.await()?.let(::putAll)
                        serienStream.await()?.let(::putAll)
                    } else {
                        serienStream.await()?.let(::putAll)
                        burningSeries.await()?.let(::putAll)
                    }
                } else {
                    serienStream.await()?.let(::putAll)
                    burningSeries.await()?.let(::putAll)
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
        season = request.season
    )

    private suspend fun series(
        request: Show.EpisodeRequest,
        searchItem: SerienStreamItem
    ): Boolean = serienStreamEpisodeManager.episodeAvailable(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season
    )

    private suspend fun streams(
        request: Show.EpisodeRequest,
        searchItem: SearchItem
    ): Map<BSLang, List<String>> = burningSeriesEpisodeManager.episodeStreams(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season
    )

    private suspend fun streams(
        request: Show.EpisodeRequest,
        searchItem: SerienStreamItem
    ): Map<SerienStreamLang, List<String>> = serienStreamEpisodeManager.episodeStreams(
        show = searchItem,
        episodeNumber = request.episodeNumber,
        season = request.season
    )

    data class EpisodeKey(
        val showId: Int,
        val season: Int,
        val episode: Int
    )
}