package de.datlag.model.video

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Obfuscate
data class DownloadVideo(
    @SerialName("success") val success: Boolean = false,
    @SerialName("url") val url: String? = null
) : Parcelable