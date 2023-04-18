@file:Suppress("NewApi")

package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.scraper.Constants
import dev.datlag.burningseries.scraper.bs.JvmBsScraper
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.*
import javax.imageio.ImageIO

actual object CoverScraper {

    init {
        JvmBsScraper.coverBlock = { cover, isNsfw ->
            getCover(cover, isNsfw)
        }
    }

    actual suspend fun getCover(cover: String?, isNsfw: Boolean): Pair<Cover, Boolean> {
        fun imgToBase64(image: RenderedImage, format: String): String {
            val output = ByteArrayOutputStream()
            val base64 = Base64.getEncoder().wrap(output)
            return try {
                ImageIO.write(image, format, base64)
                try {
                    base64.flush()
                    output.flush()
                } catch (ignored: Throwable) { }
                output.toString()
            } catch (ignored: Throwable) {
                String()
            } finally {
                output.close()
                base64.close()
            }
        }

        val image = if (cover != null) {
            val url = try {
                URI.create(Constants.getBurningSeriesLink(cover)).toURL()
            } catch (e: Throwable) {
                null
            }
            try {
                ImageIO.read(url)
            } catch (ignored: Throwable) {
                try {
                    ImageIO.read(URI.create(Constants.getBurningSeriesLink(cover, true)).toURL())
                } catch (ignored: Throwable) {
                    null
                }
            }
        } else {
            null
        }

        val base64 = image?.let {
            imgToBase64(it, cover!!.substringAfterLast('.'))
        }

        return Cover(cover ?: String(), base64 ?: String(), String()) to isNsfw
    }

}