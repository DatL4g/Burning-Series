package de.datlag.network.video

import de.datlag.model.jsonbase.Stream
import de.datlag.model.video.VideoStream
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Obfuscate
class VideoRepository @Inject constructor(
    val scraper: VideoScraper
) {

    fun getVideoSources(list: List<Stream>): Flow<List<VideoStream>> = flow {
        coroutineScope {
            emit(list.map {
                async {
                    scraper.scrapeVideosFrom(it.hoster, it.url)
                }
            }.awaitAll().filterNotNull().sortedBy { it.hoster })
        }
    }.flowOn(Dispatchers.IO)
}