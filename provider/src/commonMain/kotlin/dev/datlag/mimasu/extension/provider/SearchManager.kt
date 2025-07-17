package dev.datlag.mimasu.extension.provider

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.burningseries.BSSearchManager
import dev.datlag.mimasu.extension.provider.model.MatchedShowResults
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.CombinedSearchManager
import dev.datlag.mimasu.extension.provider.serienstream.createAniWorld
import dev.datlag.mimasu.extension.provider.serienstream.createSerienStream
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem as SerienStreamItem

class SearchManager(
    val httpClient: HttpClient,
    val fallbackClient: HttpClient?,
) {

    private val serienStream = ktorfit {
        baseUrl(SerienStreamItem.SerienStream.BASE_URL)
        httpClient(httpClient)
    }.createSerienStream()

    private val fallbackSerienStream = fallbackClient?.let {
        ktorfit {
            baseUrl(SerienStreamItem.SerienStream.BASE_URL)
            httpClient(it)
        }.createSerienStream()
    }

    private val aniWorld = ktorfit {
        baseUrl(SerienStreamItem.AniWorld.BASE_URL)
        httpClient(httpClient)
    }.createAniWorld()

    private val fallbackAniWorld = fallbackClient?.let {
        ktorfit {
            baseUrl(SerienStreamItem.AniWorld.BASE_URL)
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
        fallbackAniWorld = fallbackAniWorld,
        httpClient = httpClient,
        fallbackClient = fallbackClient
    )

    private val mappings = mutableMapOf<Int, MatchedShowResults>()

    suspend fun initialize() = coroutineScope {
        val burningSeriesSearchIndex = async {
            burningSeriesSearchManager.initialize()
        }
        val serienStreamSearchIndex = async {
            serienStreamSearchManager.initializeCombined()
        }

        val burningSeriesResult = burningSeriesSearchIndex.await()
        val serienStreamResult = serienStreamSearchIndex.await()

        return@coroutineScope burningSeriesResult.size + serienStreamResult.size
    }

    fun matchedShowResults(showId: Int): MatchedShowResults? {
        return mappings[showId]?.takeUnless { it.isEmpty() }
    }

    suspend fun search(request: Show.Request): Int? = coroutineScope {
        val id = request.tmdbId ?: return@coroutineScope null

        mappings[id]?.let {
            if (!it.isEmpty()) {
                return@coroutineScope id
            }
        }

        val serienStream = async {
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
            )?.also {
                mappings[id]?.plus(MatchedShowResults(serienStream = it))
            }
        }
        val burningSeries = async {
            burningSeriesSearchManager.search(
                tmdbId = request.tmdbId,
                tokens = listOf(
                    request.tokenResult,
                    request.originalTokenResult
                ),
                releaseYear = request.firstReleaseYear,
                isAnimation = request.isAnimation
            )?.also {
                mappings[id]?.plus(MatchedShowResults(burningSeries = it))
            }
        }

        MatchedShowResults(
            burningSeries = burningSeries.await(),
            serienStream = serienStream.await()
        ).takeUnless { it.isEmpty() }?.let {
            mappings[id]?.plus(it) ?: mappings.put(id, it)
        }
        return@coroutineScope if (mappings[id]?.takeUnless { it.isEmpty() } != null) {
            id
        } else {
            null
        }
    }
}