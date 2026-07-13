package dev.datlag.mimasu.extension.provider

import de.jensklingenberg.ktorfit.ktorfit
import dev.datlag.mimasu.extension.kache.CachePool
import dev.datlag.mimasu.extension.matcher.MatchResult
import dev.datlag.mimasu.extension.provider.burningseries.BSSearchManager
import dev.datlag.mimasu.extension.provider.model.MatchedMovieResults
import dev.datlag.mimasu.extension.provider.model.MatchedShowResults
import dev.datlag.mimasu.extension.provider.model.Movie
import dev.datlag.mimasu.extension.provider.model.Show
import dev.datlag.mimasu.extension.provider.serienstream.CombinedSearchManager
import dev.datlag.mimasu.extension.provider.serienstream.createAniWorld
import dev.datlag.mimasu.extension.provider.serienstream.createSerienStream
import dev.datlag.mimasu.extension.provider.streamkiste.Streamkiste
import dev.datlag.mimasu.extension.provider.streamkiste.StreamkisteSearchManager
import dev.datlag.mimasu.extension.provider.streamkiste.createStreamkiste
import dev.datlag.tooling.async.suspendCatching
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import dev.datlag.mimasu.extension.provider.serienstream.model.SearchItem as SerienStreamItem

class SearchManager(
    val httpClient: HttpClient,
    val fallbackClient: HttpClient?,
) : CachePool {

    private val serienStream = ktorfit {
        httpClient(httpClient)
    }.createSerienStream()

    private val fallbackSerienStream = fallbackClient?.let {
        ktorfit {
            httpClient(it)
        }.createSerienStream()
    }

    private val aniWorld = ktorfit {
        httpClient(httpClient)
    }.createAniWorld()

    private val fallbackAniWorld = fallbackClient?.let {
        ktorfit {
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

    private val streamkiste = ktorfit {
        baseUrl(Streamkiste.BASE_URL)
        httpClient(httpClient)
    }.createStreamkiste()

    private val fallbackStreamkiste = fallbackClient?.let {
        ktorfit {
            baseUrl(Streamkiste.BASE_URL)
            httpClient(it)
        }.createStreamkiste()
    }

    private val streamkisteSearchManager = StreamkisteSearchManager(
        streamkiste = streamkiste,
        fallbackStreamkiste = fallbackStreamkiste
    )

    private val seriesMappings = mutableMapOf<Int, MatchedShowResults>()
    private val movieMappings = mutableMapOf<Int, MatchedMovieResults>()

    override suspend fun clear(): Boolean {
        val result = suspendCatching {
            seriesMappings.clear()
        }.isSuccess
                && burningSeriesSearchManager.clear()
                && serienStreamSearchManager.clear()
                && streamkisteSearchManager.clear()

        initialize()
        return result
    }

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
        return seriesMappings[showId]?.takeUnless { it.isEmpty() }
    }

    fun matchedMovieResults(movieId: Int): MatchedMovieResults? {
        return movieMappings[movieId]?.takeUnless { it.isEmpty() }
    }

    suspend fun search(request: Show.Request): Int? = coroutineScope {
        val id = request.tmdbId ?: return@coroutineScope null

        seriesMappings[id]?.let {
            if (!it.isEmpty()) {
                return@coroutineScope id
            }
        }

        val serienStream = async {
            serienStreamSearchManager.search(
                tmdbId = request.tmdbId,
                titles = setOfNotNull(
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
                seriesMappings[id]?.plus(MatchedShowResults(serienStream = it))
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
                seriesMappings[id]?.plus(MatchedShowResults(burningSeries = it))
            }
        }
        val streamkiste = async {
            streamkisteSearchManager.searchSeries(
                tmdbId = request.tmdbId,
                titles = setOfNotNull(
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
                seriesMappings[id]?.plus(MatchedShowResults(streamkiste = it))
            }
        }

        MatchedShowResults(
            burningSeries = burningSeries.await(),
            serienStream = serienStream.await(),
            streamkiste = streamkiste.await()
        ).takeUnless { it.isEmpty() }?.let {
            seriesMappings[id]?.plus(it) ?: seriesMappings.put(id, it)
        }
        return@coroutineScope if (seriesMappings[id]?.takeUnless { it.isEmpty() } != null) {
            id
        } else {
            null
        }
    }

    suspend fun search(request: Movie.Request): Int? = coroutineScope {
        val id = request.tmdbId ?: return@coroutineScope null

        movieMappings[id]?.let {
            if (!it.isEmpty()) {
                return@coroutineScope id
            }
        }

        val streamkiste = async {
            streamkisteSearchManager.searchMovie(
                tmdbId = request.tmdbId,
                titles = setOfNotNull(
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
                movieMappings[id]?.plus(MatchedMovieResults(streamkiste = it))
            }
        }

        MatchedMovieResults(
            streamkiste = streamkiste.await()
        ).takeUnless { it.isEmpty() }?.let {
            movieMappings[id]?.plus(it) ?: movieMappings.put(id, it)
        }

        return@coroutineScope if (movieMappings[id]?.takeUnless { it.isEmpty() } != null) {
            id
        } else {
            null
        }
    }
}