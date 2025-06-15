package dev.datlag.mimasu.extension.provider

import co.touchlab.kermit.Logger
import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.provider.burningseries.BSEpisodeManager
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.MatchedShowResults
import dev.datlag.mimasu.extension.provider.model.Show
import io.ktor.client.HttpClient
import kotlin.time.Duration.Companion.hours

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

        episodeKache.getIfAvailable(episodeKey)?.let {
            return it
        }
        val burningSeries = matchedShowResults.burningSeries ?: return false
        return series(
            request = request,
            searchItem = burningSeries.data
        )
    }

    suspend fun episodeStreams(
        matchedShowResults: MatchedShowResults,
        request: Show.EpisodeRequest
    ): Map<Show.Response.SourceInfo, List<String>> {
        val burningSeries = matchedShowResults.burningSeries ?: return run {
            Logger.e("No Burning Series Result")
            emptyMap()
        }
        return streams(
            request = request,
            searchItem = burningSeries.data
        ).mapNotNull { (key, value) ->
            key to TestVideo.filter(value).ifEmpty {
                Logger.e("Empty streams after filtering")
                return@mapNotNull null
            }
        }.associate { (k, v) ->
            Show.Response.SourceInfo(
                sourceTitle = BurningSeries.TITLE,
                sourceLocale = k,
                locale = k
            ) to v
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

    private suspend fun streams(
        request: Show.EpisodeRequest,
        searchItem: SearchItem
    ): Map<String, List<String>> = burningSeriesEpisodeManager.episodeStreams(
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