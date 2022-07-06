package de.datlag.model.video

import android.os.Parcelable
import de.datlag.model.burningseries.stream.StreamConfig
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class ScrapeHoster(
    @SerialName("href") val href: String = String(),
    @SerialName("url") val url: String = String(),
    @SerialName("config") val config: StreamConfig? = null
) : Parcelable