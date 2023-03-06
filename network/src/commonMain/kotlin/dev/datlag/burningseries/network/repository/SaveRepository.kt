package dev.datlag.burningseries.network.repository

import com.hadiyarajesh.flower_core.Resource
import com.hadiyarajesh.flower_core.networkResource
import dev.datlag.burningseries.model.InsertStream
import dev.datlag.burningseries.model.SaveInfo
import dev.datlag.burningseries.model.ScrapedHoster
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.network.BurningSeries
import dev.datlag.burningseries.network.Status
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import dev.datlag.burningseries.network.common.Dispatchers
import dev.datlag.burningseries.network.video.Scraper

class SaveRepository(
    private val api: BurningSeries,
    private val scraper: Scraper? = null
) {

    private val _scrapedHoster: MutableStateFlow<ScrapedHoster?> = MutableStateFlow(null)
    private val _saveScrapedHoster: Flow<Resource<InsertStream>> = _scrapedHoster.debounce(500).distinctUntilChanged().transformLatest {
        if (it != null) {
            return@transformLatest emitAll(networkResource(
                makeNetworkRequest = {
                    api.save(it)
                }
            ))
        }
    }.flowOn(Dispatchers.IO)

    private val _status = _saveScrapedHoster.transform {
        return@transform emit(it.status)
    }.flowOn(Dispatchers.IO)

    val status = _status.transform {
        return@transform emit(Status.create(it, true))
    }.flowOn(Dispatchers.IO)

    private val stream: MutableStateFlow<VideoStream?> = MutableStateFlow(null)
    private val scrapedEpisodeHref: MutableStateFlow<String> = MutableStateFlow(String())

    val saveInfo: Flow<SaveInfo> = _status.transform {
        when (it) {
            is Resource.Status.Success -> return@transform emit(SaveInfo(
                it.data.failed <= 0,
                scrapedEpisodeHref.value,
                stream.value
            ))
            is Resource.Status.Error -> return@transform emit(
                SaveInfo(
                false,
                scrapedEpisodeHref.value,
                stream.value
            )
            )
            else -> { }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun save(scrapedHoster: ScrapedHoster) = withContext(Dispatchers.IO) {
        // resetting values, in case the user saved one previously
        this@SaveRepository.stream.emit(null)
        this@SaveRepository.scrapedEpisodeHref.emit(String())

        // save on server
        this@SaveRepository._scrapedHoster.emit(scrapedHoster)

        // set scraped data
        this@SaveRepository.scrapedEpisodeHref.emit(scrapedHoster.href)
        this@SaveRepository.stream.emit(scraper?.scrapeVideosFrom(scrapedHoster.toHosterStream()))
    }
}