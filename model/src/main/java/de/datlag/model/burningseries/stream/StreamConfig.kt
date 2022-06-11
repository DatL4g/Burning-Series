package de.datlag.model.burningseries.stream

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class StreamConfig(
    @SerialName("throwback") val throwback: StreamClip = StreamClip(),
    @SerialName("intro") val intro: StreamClip = StreamClip(),
    @SerialName("outro") val outro: StreamClip = StreamClip()
) : Parcelable
