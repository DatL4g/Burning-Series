package dev.datlag.mimasu.extension.provider

import co.touchlab.kermit.Logger
import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.provider.burningseries.BSSearchManager
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.CombinedSearchManager
import dev.datlag.mimasu.extension.provider.serienstream.createAniWorld
import dev.datlag.mimasu.extension.provider.serienstream.createSerienStream
import io.ktor.client.HttpClient

class SearchManager(
    val httpClient: HttpClient,
    val fallbackClient: HttpClient?,
) {

    private val serienStream = ktorfit {
        baseUrl(SERIENSTREAM_BASE_URL)
        httpClient(httpClient)
    }.createSerienStream()

    private val fallbackSerienStream = fallbackClient?.let {
        ktorfit {
            baseUrl(SERIENSTREAM_BASE_URL)
            httpClient(it)
        }.createSerienStream()
    }

    private val aniWorld = ktorfit {
        baseUrl(ANIWORLD_BASE_URL)
        httpClient(httpClient)
    }.createAniWorld()

    private val fallbackAniWorld = fallbackClient?.let {
        ktorfit {
            baseUrl(ANIWORLD_BASE_URL)
            httpClient(it)
        }.createAniWorld()
    }

    private val burningSeriesSearchManager = BSSearchManager(
        httpClient = httpClient,
        fallbackClient = fallbackClient
    )

    private val serienStreamSearchManager = CombinedSearchManager(
        serienStream = serienStream,
        fallbackSerienStream = fallbackSerienStream,
        aniWorld = aniWorld,
        fallbackAniWorld = fallbackAniWorld
    )

    suspend fun search(request: Show.Request): Int? {
        serienStreamSearchManager.search(
            tmdbId = request.tmdbId,
            titles = listOfNotNull(
                request.title,
                request.originalTitle
            ),
            tokens = listOf(
                request.tokenResult,
                request.originalTokenResult
            ),
            releaseYear = request.firstReleaseYear,
            isAnimation = request.isAnimation
        )
        Logger.e("Start searching for burning series")
        return burningSeriesSearchManager.search(
            tmdbId = request.tmdbId,
            tokens = listOf(
                request.tokenResult,
                request.originalTokenResult
            ),
            releaseYear = request.firstReleaseYear,
            isAnimation = request.isAnimation
        )
    }

    data class AllShowResults(
        val burningSeries: Int?,
        val serienStream: Int?
    )

    companion object {
        private const val SERIENSTREAM_BASE_URL = "https://s.to/"
        private const val ANIWORLD_BASE_URL = "https://aniworld.to/"
    }
}