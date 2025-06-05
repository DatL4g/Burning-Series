package dev.datlag.mimasu.extension.provider

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.matcher.SearchMatcher
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.Movie
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.createAniWorld
import dev.datlag.mimasu.extension.provider.serienstream.createSerienStream
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    private val burningSeriesMappings = mutableMapOf<Int, SearchItem>()

    suspend fun search(request: Movie.Request) {
        // ToDo("movies will work a bit different")
    }

    suspend fun search(request: Show.Request): Int? = coroutineScope {
        burningSeriesMappings[request.tmdbId]?.let {
            return@coroutineScope request.tmdbId
        }

        val searchItems = BurningSeries.search(httpClient).ifEmpty {
            fallbackClient?.let { BurningSeries.search(fallbackClient) }
        }?.ifEmpty { null } ?: return@coroutineScope null

        val requestReleaseYear = request.firstReleaseYear
        val matched = searchItems.map { item -> async {
            val itemTokenList = listOf(
                item.tokenResult,
                *item.alternativeTokenResults.toTypedArray()
            )
            val similarity = listOf(
                request.tokenResult,
                request.originalTokenResult
            ).map { tokens -> async {
                itemTokenList.maxOfOrNull { searchToken ->
                    SearchMatcher.calculateSymmetricSimilarity(searchToken, tokens)
                }
            } }.awaitAll().filterNotNull().max()

            Pair(item, similarity)
        } }.awaitAll().filter {
            it.second > 0.1
        }.map { (item, score) ->
            if (requestReleaseYear != null && item.releaseYear != null && requestReleaseYear == item.releaseYear) {
                return@map Pair(item, score + 0.15)
            }
            Pair(item, score)
        }.filter {
            it.second > 0.3
        }

        val found = matched.maxByOrNull { it.second }

        return@coroutineScope found?.first?.let { item ->
            request.tmdbId?.also {
                burningSeriesMappings[it] = item
            }
        }
    }

    companion object {
        private const val SERIENSTREAM_BASE_URL = "https://s.to/"
        private const val ANIWORLD_BASE_URL = "https://aniworld.to/"
    }
}