package dev.datlag.mimasu.extension.provider.burningseries

import co.touchlab.kermit.Logger
import dev.datlag.mimasu.extension.firebase.FirebaseWrapper
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.burningseries.model.Series
import io.ktor.client.HttpClient

class BSEpisodeManager(
    private val httpClient: HttpClient,
    private val fallbackClient: HttpClient?,
    private val firebaseWrapper: FirebaseWrapper?
) {

    private val mappings = mutableMapOf<String, Series>()

    suspend fun episode(
        show: SearchItem,
        episodeNumber: Int?,
        season: Int?
    ) {
        val link = if (show.href.endsWith('/')) {
            "${show.href}${season}"
        } else {
            "${show.href}/${season}"
        }

        val series = mappings[link] ?: BurningSeries.series(httpClient, link)?.also {
            mappings[link] = it
        } ?: return

        val requestedEpisode = series.episodes.firstOrNull { it.number == episodeNumber } ?: return
        val wrapper = firebaseWrapper ?: return
        val hosterUrls = wrapper.store.streams(requestedEpisode.hoster.toList())

        Logger.e("Requested Episode [$episodeNumber]: $$hosterUrls")
    }

}