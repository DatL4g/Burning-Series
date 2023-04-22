package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.ApiSuccessResponse
import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.dbBoundResource
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.model.Genre
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
    private val client: HttpClient
) {

    val allState: MutableStateFlow<List<Genre>> = MutableStateFlow(emptyList())

    private val all: Flow<Resource<List<Genre>>> = dbBoundResource(
        makeNetworkRequest = {
            BsScraper.client(client).getAll("andere-serien")?.let { genres ->
                ApiSuccessResponse(genres, emptySet())
            } ?: api.all()
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
                emptyList()
            }
            is Resource.Status.Success -> {
                it.data
            }
            is Resource.Status.Error -> {
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