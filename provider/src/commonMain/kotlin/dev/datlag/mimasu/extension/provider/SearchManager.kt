package dev.datlag.mimasu.extension.provider

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.provider.model.Movie
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

    suspend fun search(request: Movie.Request) {
        // ToDo("movies will work a bit different")
    }

    companion object {
        private const val SERIENSTREAM_BASE_URL = "https://s.to/"
        private const val ANIWORLD_BASE_URL = "https://aniworld.to/"
    }
}