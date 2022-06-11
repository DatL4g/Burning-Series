package de.datlag.model.video

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class InsertStream(
    @SerialName("inserted") val inserted: Long,
    @SerialName("updated") val updated: Long,
    @SerialName("failed") val failed: Long
) : Parcelable
