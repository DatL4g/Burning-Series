package dev.datlag.mimasu.extension.provider.burningseries

import co.touchlab.kermit.Logger
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
    ): Collection<String> {
        val link = if (show.href.endsWith('/')) {
            "${show.href}${season}"
        } else {
            "${show.href}/${season}"
        }

        val series = mappings[link] ?: BurningSeries.series(httpClient, link)?.also {
            mappings[link] = it
        } ?: return emptyList()

        val requestedEpisode = series.episodes.firstOrNull { it.number == episodeNumber } ?: return emptyList()
        val wrapper = firebaseWrapper ?: return emptyList()
        val hosterUrls = wrapper.store.streams(requestedEpisode.hoster.toList())
        val streamingUrls = stream(httpClient, hosterUrls)

        return streamingUrls
    }

    suspend fun stream(client: HttpClient, urls: Collection<String>) = coroutineScope {
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