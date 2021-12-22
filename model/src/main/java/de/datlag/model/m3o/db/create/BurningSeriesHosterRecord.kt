package de.datlag.model.m3o.db.create

import android.os.Parcelable
import de.datlag.model.jsonbase.Stream
import de.datlag.model.video.ScrapeHoster
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class BurningSeriesHosterRecord(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("embedded") val embedded: Boolean = false
) : Parcelable {
    fun toStream(hoster: String): Stream {
        return Stream(hoster, url, embedded)
    }

    companion object {
        fun fromStream(href: String, stream: Stream): BurningSeriesHosterRecord {
            return BurningSeriesHosterRecord(href, stream.url, stream.embed)
        }

        fun fromScraped(scraped: ScrapeHoster): BurningSeriesHosterRecord {
            return BurningSeriesHosterRecord(scraped.id, scraped.url, scraped.embed)
        }
    }
}