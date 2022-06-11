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
    @SerialName("href") val href: String = String(),
    @SerialName("url") val url: String = String()
) : Parcelable