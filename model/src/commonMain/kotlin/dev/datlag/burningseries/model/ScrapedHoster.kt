package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ScrapedHoster(
    @SerialName("href") val href: String,
    @SerialName("url") val url: String
) : Parcelable {

    fun toHosterStream(): HosterStream {
        var hoster = href
        if (hoster.endsWith('/')) {
            hoster = hoster.substring(0, hoster.length - 2)
        }
        hoster = hoster.substringAfterLast('/')

        val defaultSkipConfig = HosterStream.Config.Info()

        return HosterStream(
            hoster,
            url,
            HosterStream.Config(
                defaultSkipConfig,
                defaultSkipConfig,
                defaultSkipConfig
            )
        )
    }
}
