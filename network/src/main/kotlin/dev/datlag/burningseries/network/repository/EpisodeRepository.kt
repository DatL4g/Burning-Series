package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.VideoScraper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class EpisodeRepository(
    private val api: BurningSeries
) {

    private val episode: MutableStateFlow<Series.Episode?> = MutableStateFlow(null)
    private val _hosterStreams: Flow<Resource<List<HosterStream>>> = episode.debounce(500).distinctUntilChanged().transformLatest {
        if (it != null) {
            return@transformLatest emitAll(networkResource(
                makeNetworkRequest = {
                    api.hosterStreams(it.hoster.map { hoster -> hoster.href })
                }
            ).distinctUntilChanged())
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _hosterStreams.transformLatest {
        return@transformLatest emit(it.status)
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val status = _status.transformLatest {
        return@transformLatest emit(when (it) {
            is Resource.Status.Loading -> Status.LOADING
            is Resource.Status.Error -> Status.ERROR
            is Resource.Status.EmptySuccess -> Status.ERROR
            is Resource.Status.Success -> Status.SUCCESS
        })
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val hosterStreams = _status.transformLatest {
        return@transformLatest emit(when (it) {
            is Resource.Status.Loading -> {
                it.data
            }
            is Resource.Status.Error -> {
                it.data?.ifEmpty {
                    emptyList()
                } ?: emptyList()
            }
            is Resource.Status.EmptySuccess -> {
                emptyList()
            }
            is Resource.Status.Success -> {
                it.data
            }
        })
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val streams: Flow<List<VideoStream>> = hosterStreams.transformLatest {
        if (it != null) {
            return@transformLatest emit(withContext(Dispatchers.IO) {
                it.map { hoster -> async {
                    VideoScraper.scrapeVideosFrom(hoster)
                } }.awaitAll().filterNotNull()
            })
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    suspend fun loadHosterStreams(episode: Series.Episode) {
        this.episode.emit(episode)
    }

    sealed class Status {
        object LOADING : Status()
        object ERROR : Status()
        object SUCCESS : Status()
    }
}