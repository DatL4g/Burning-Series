package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.ApiSuccessResponse
import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.dbBoundResource
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.LoggingMode
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.bs.BsScraper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import dev.datlag.burningseries.network.common.Dispatchers
import io.ktor.client.*

class GenreRepository(
    private val api: BurningSeries,
    override val logger: ActionLogger,
    private val client: HttpClient
) : LogRepository {

    override val mode: Int = LoggingMode.SEARCH

    val allState: MutableStateFlow<List<Genre>> = MutableStateFlow(emptyList())

    private val all: Flow<Resource<List<Genre>>> = dbBoundResource(
        makeNetworkRequest = {
            info("Loading genre info")
            BsScraper.client(client).logger(logger).getAll("andere-serien")?.let { genres ->
                ApiSuccessResponse(genres, emptySet())
            } ?: run {
                warning("Could not scrape genres on-device")
                api.all()
            }
        },
        fetchFromLocal = { allState },
        shouldMakeNetworkRequest = { it.isNullOrEmpty() },
        saveResponseData = { allState.emit(it) }
    ).flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.Lazily, Resource.loading())

    private val _status = all.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(Status.create(it))
    }

    private val allGenres = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> {
                it.data ?: emptyList()
            }
            is Resource.Status.EmptySuccess -> {
                warning("Got empty response when loading genres")
                emptyList()
            }
            is Resource.Status.Success -> {
                it.data
            }
            is Resource.Status.Error -> {
                error("Could not load genres: (${it.statusCode}) ${it.message}")
                it.data ?: emptyList()
            }
        })
    }.flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.Lazily, emptyList())

    private val selectedGenreIndex = MutableStateFlow<Int>(0)
    private val _searchItems = MutableStateFlow<List<Genre.Item>>(emptyList())
    val searchItems: Flow<List<Genre.Item>> = _searchItems

    val currentGenre = combine(allGenres, selectedGenreIndex) { genres, index ->
        genres.getOrNull(index) ?: genres.firstOrNull()
    }.flowOn(Dispatchers.IO)

    suspend fun nextGenre() = withContext(Dispatchers.IO) {
        var index = selectedGenreIndex.value + 1

        if (index >= allGenres.value.size) {
            index = 0
        }
        selectedGenreIndex.emit(index)
    }

    suspend fun previousGenre() = withContext(Dispatchers.IO) {
        var index = selectedGenreIndex.value - 1

        if (index <= -1) {
            index = allGenres.value.size - 1
        }
        selectedGenreIndex.emit(index)
    }

    suspend fun searchSeries(value: String) = withContext(Dispatchers.IO) {
        if (value.isEmpty()) {
            _searchItems.emit(emptyList())
        } else {
            _searchItems.emit(allGenres.value.flatMap { it.items }.map {
                when {
                    it.title.equals(value, true) -> it to 1.0
                    it.title.startsWith(value, true) -> it to 0.95
                    it.title.contains(value, true) -> it to 0.9
                    else -> it to JaroWinkler.distance(it.title, value)
                }
            }.filter { it.second > 0.85 }.sortedByDescending { it.second }.map { it.first })
        }
    }
}