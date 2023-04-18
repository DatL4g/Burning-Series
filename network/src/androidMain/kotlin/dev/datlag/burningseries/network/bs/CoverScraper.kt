package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.common.encodeBase64
import dev.datlag.burningseries.scraper.Constants
import java.net.URI

actual object CoverScraper {

    actual suspend fun getCover(
        cover: String?,
        isNsfw: Boolean
    ): Pair<Cover, Boolean> {
        val imageByteArray = if (cover != null) {
            val url = try {
                URI.create(Constants.getBurningSeriesLink(cover)).toURL()
            } catch (e: Throwable) {
                null
            }
            val connection = try {
                url?.openConnection()
            } catch (ignored: Throwable) {
                null
            }
            val stream = try {
                connection?.getInputStream()
            } catch (ignored: Throwable) {
                null
            }
            stream?.readBytes()
        } else {
            null
        }

        val base64 = imageByteArray?.let {
            it.encodeBase64()
        }

        return Cover(cover ?: String(), base64 ?: String(), String()) to isNsfw
    }


}