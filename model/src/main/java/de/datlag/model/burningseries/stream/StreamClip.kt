package de.datlag.model.burningseries.stream

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class StreamClip(
    @SerialName("start") var start: Long? = null,
    @SerialName("end") var end: Long? = null
) : Parcelable
