package de.datlag.model.burningseries.series

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class EpisodeData(
    @SerialName("text") val text: String = String(),
    @SerialName("href") val href: String = String()
) : Parcelable
