package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.ActionLogger
import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.model.algorithm.MD5
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.JsonBase
import dev.datlag.burningseries.network.Status
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import dev.datlag.burningseries.network.common.Dispatchers
import dev.datlag.burningseries.network.video.Scraper

class EpisodeRepository(
    private val api: BurningSeries,
    private val jsonBase: JsonBase,
    override val logger: ActionLogger,
    private val scraper: Scraper? = null
) : LogRepository {

    override val mode: Int = 4

    private val episode: MutableStateFlow<Series.Episode?> = MutableStateFlow(null)
    private val _hosterStreams: Flow<Resource<List<HosterStream>>> = episode.debounce(500).distinctUntilChanged().transformLatest {
        if (it != null) {
            return@transformLatest emitAll(networkResource(
                makeNetworkRequest = {
                    val data = it.hoster.map { hoster -> hoster.href }
                    info("Load streams $data")
                    api.hosterStreams(data)
                }
            ).distinctUntilChanged())
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _hosterStreams.transformLatest {
        return@transformLatest emit(it.status)
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val status = _status.transformLatest {
        return@transformLatest emit(Status.create(it, true))
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val hosterStreams = _status.transformLatest {
        return@transformLatest emit(when (it) {
            is Resource.Status.Loading -> {
                it.data
            }
            is Resource.Status.Error -> {
                warning("Could not load streams: (${it.statusCode}) ${it.message}")
                it.data?.ifEmpty {
                    backupStreams(episode.value)
                } ?: backupStreams(episode.value)
            }
            is Resource.Status.EmptySuccess -> {
                warning("Got empty response when loading streams")
                backupStreams(episode.value)
            }
            is Resource.Status.Success -> {
                it.data.ifEmpty {
                    backupStreams(episode.value)
                }
            }
        })
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    val streams: Flow<List<VideoStream>> = hosterStreams.transformLatest {
        if (it != null) {
            return@transformLatest emit(withContext(Dispatchers.IO) {
                it.map { hoster -> async {
                    scraper?.scrapeVideosFrom(hoster)
                } }.awaitAll().filterNotNull()
            })
        }
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    suspend fun loadHosterStreams(episode: Series.Episode) {
        info("Load streams for ${episode.title}")
        info( episode.hoster.joinToString(separator = "\n") { it.href })
        info( "------------------------------")
        this.episode.emit(episode)
    }

    private suspend fun backupStreams(episode: Series.Episode?): List<HosterStream> = coroutineScope {
        warning( "Could not load streams, trying backup strategy")
        if (episode == null) {
            error("Streams could not be loaded, episode null")
            return@coroutineScope emptyList()
        }
        return@coroutineScope episode.hoster.map { hoster ->
            async {
                networkResource(makeNetworkRequest = {
                    jsonBase.burningSeriesCaptcha(MD5.hexString(hoster.href))
                }).mapNotNull {
                    when (it.status) {
                        is Resource.Status.Loading -> null
                        else -> it
                    }
                }.map {
                    when (it.status) {
                        is Resource.Status.Success -> {
                            HosterStream(hoster.title, (it.status as Resource.Status.Success).data.url)
                        }
                        else -> {
                            error( "Could not load streams using backup strategy either")
                            null
                        }
                    }
                }.first()
            }
        }.awaitAll().filterNotNull()
    }
}