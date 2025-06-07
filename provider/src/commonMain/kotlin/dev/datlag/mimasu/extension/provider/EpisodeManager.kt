package dev.datlag.mimasu.extension.provider

import co.touchlab.kermit.Logger
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.provider.burningseries.BSEpisodeManager
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.MatchedShowResults
import dev.datlag.mimasu.extension.provider.model.Show
import io.ktor.client.HttpClient

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

    suspend fun watchInfo(matchedShowResults: MatchedShowResults, request: Show.EpisodeRequest) {
        val burningSeries = matchedShowResults.burningSeries ?: return

        Logger.e("Requested Episode [${request.episodeNumber}] ${request.episodeTitle}")
        series(
            request = request,
            searchItem = burningSeries.data
        )
    }

    private suspend fun series(
        request: Show.EpisodeRequest,
        searchItem: SearchItem
    ) {
        burningSeriesEpisodeManager.episode(
            show = searchItem,
            episodeNumber = request.episodeNumber,
            season = request.season
        )
    }

}