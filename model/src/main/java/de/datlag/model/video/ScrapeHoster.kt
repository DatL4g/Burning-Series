package de.datlag.model.video

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class ScrapeHoster(
    @SerialName("id") val id: String = String(),
    @SerialName("url") val url: String = String(),
    @SerialName("embed") val embed: Boolean = false
) : Parcelable