package dev.datlag.mimasu.extension.provider

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.provider.burningseries.BurningSeries
import dev.datlag.mimasu.extension.provider.burningseries.model.SearchItem
import dev.datlag.mimasu.extension.provider.model.Movie
import dev.datlag.mimasu.extension.provider.model.Show
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

    private val aniworld = ktorfit {
        baseUrl(ANIWORLD_BASE_URL)
        httpClient(httpClient)
    }.createAniWorld()

    private val fallbackAniworld = fallbackClient?.let {
        ktorfit {
            baseUrl(ANIWORLD_BASE_URL)
            httpClient(it)
        }.createAniWorld()
    }

    private val burningSeriesMappings = mutableMapOf<Int, SearchItem>()

    suspend fun search(request: Movie.Request) {
        // ToDo("movies will work a bit different")
    }

    suspend fun search(request: Show.Request): Int? {
        burningSeriesMappings[request.tmdbId]?.let {
            return request.tmdbId
        }

        val searchItems = BurningSeries.search(httpClient).ifEmpty {
            fallbackClient?.let { BurningSeries.search(fallbackClient) }
        }?.ifEmpty { null } ?: return null

        val matching = searchItems.firstOrNull {
            it.title.equals(request.title, ignoreCase = true)
        } ?: searchItems.firstOrNull {
            it.title.equals(request.originalTitle, ignoreCase = true)
        }
        return matching?.let { item ->
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