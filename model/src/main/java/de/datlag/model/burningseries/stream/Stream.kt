package de.datlag.model.burningseries.stream

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class Stream(
    @SerialName("_id") val href: String,
    @SerialName("hoster") val hoster: String,
    @SerialName("url") val url: String,
    @SerialName("config") val config: StreamConfig = StreamConfig()
) : Parcelable
