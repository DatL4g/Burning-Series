package de.datlag.network.video

import android.net.Uri
import androidx.datastore.core.DataStore
import com.hadiyarajesh.flower.Resource
import com.hadiyarajesh.flower.networkResource
import de.datlag.datastore.SettingsPreferences
import de.datlag.model.jsonbase.Stream
import de.datlag.model.video.VideoStream
import de.datlag.network.common.toInt
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Named

@Obfuscate
class VideoRepository @Inject constructor(
    val scraper: VideoScraper,
    @Named("wrapApiToken") val wrapApiToken: String,
    val service: DownloadVideo
) {

    @Inject
    lateinit var settingsDataStore: DataStore<SettingsPreferences>

    fun getVideoSources(list: List<Stream>): Flow<List<VideoStream>> = flow {
        coroutineScope {
            emit(list.map {
                async {
                    val scraped = scraper.scrapeVideosFrom(it.url)
                    val api = if (settingsDataStore.data.first().video.advancedFetching) {
                        getDownloadVideo(it.url).first()
                    } else {
                        emptyList()
                    }
                    val completeList: MutableSet<String> = mutableSetOf()

                    completeList.addAll(scraped)
                    completeList.addAll(api)

                    if (completeList.isEmpty()) {
                        null
                    } else {
                        val preferMp4 = settingsDataStore.data.first().video.preferMp4
                        VideoStream(
                            it.hoster,
                            it.url,
                            completeList.toList().map { Uri.parse(it).buildUpon().clearQuery().toString() }.sortedWith(compareByDescending {
                                it.endsWith(if (preferMp4) ".mp4" else ".m3u8", true).toInt()
                            })
                        )
                    }
                }
            }.awaitAll().filterNotNull().sortedBy { it.hoster })
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun getDownloadVideo(url: String): Flow<List<String>> = flow {
        networkResource(fetchFromRemote = {
            service.getDownloadVideo(apiKey = wrapApiToken, site = url)
        }).collect {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    it.data?.let { safeData ->
                        if (safeData.success && !safeData.url.isNullOrEmpty()) {
                            emit(listOf(safeData.url!!))
                        } else {
                            emit(emptyList<String>())
                        }
                    }
                }
                Resource.Status.ERROR -> {
                    emit(emptyList<String>())
                }
                else -> { }
            }
        }
    }
}