package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.algorithm.Levenshtein
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class GenreRepository(
    private val api: BurningSeries
) {

    private val all: Flow<Resource<List<Genre>>> = networkResource(
        makeNetworkRequest = { api.all() }
    ).flowOn(Dispatchers.IO).stateIn(GlobalScope, SharingStarted.Lazily, Resource.loading())

    private val _status = all.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(when (it) {
            is Resource.Status.Loading -> Status.LOADING
            is Resource.Status.Error -> Status.ERROR
            is Resource.Status.EmptySuccess -> Status.SUCCESS
            is Resource.Status.Success -> Status.SUCCESS
        })
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
                    else -> {
                        val jaroWinkler = JaroWinkler.distance(it.title, value)
                        it to if (jaroWinkler < 0.85) {
                            Levenshtein.normalizedSimilarity(it.title, value).toDouble()
                        } else {
                            jaroWinkler
                        }
                    }
                }
            }.filter { it.second > 0.85 }.sortedByDescending { it.second }.map { it.first })
        }
    }

    sealed class Status {
        object LOADING : Status()
        object ERROR : Status()
        object SUCCESS : Status()
    }
}