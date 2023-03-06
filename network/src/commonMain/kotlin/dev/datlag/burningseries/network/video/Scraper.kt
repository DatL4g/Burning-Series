package dev.datlag.burningseries.network.video

import dev.datlag.burningseries.model.HosterStream
import dev.datlag.burningseries.model.VideoStream

interface Scraper {

    suspend fun scrapeVideosFrom(hosterStream: HosterStream): VideoStream?
}