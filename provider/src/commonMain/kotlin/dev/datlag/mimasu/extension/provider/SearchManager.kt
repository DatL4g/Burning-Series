package dev.datlag.mimasu.extension.provider

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.burningseries.BSSearchManager
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.CombinedSearchManager
import dev.datlag.mimasu.extension.provider.serienstream.createAniWorld
import dev.datlag.mimasu.extension.provider.serienstream.createSerienStream
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchResult
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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

    private val mappings = mutableMapOf<Int, AllShowResults>()

    suspend fun initialize() {
        burningSeriesSearchManager.initialize()
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
                mappings[id]?.plus(AllShowResults(serienStream = it))
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
                mappings[id]?.plus(AllShowResults(burningSeries = it))
            }
        }

        AllShowResults(
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

    data class AllShowResults(
        val burningSeries: MatchResult<SearchItem>? = null,
        val serienStream: MatchResult<SearchResult>? = null
    ) {

        operator fun plus(other: AllShowResults): AllShowResults = this.copy(
            burningSeries = burningSeries ?: other.burningSeries,
            serienStream = serienStream ?: other.serienStream
        )

        fun isEmpty(): Boolean {
            return burningSeries == null && serienStream == null
        }
    }

    companion object {
        private const val SERIENSTREAM_BASE_URL = "https://s.to/"
        private const val ANIWORLD_BASE_URL = "https://aniworld.to/"
    }
}