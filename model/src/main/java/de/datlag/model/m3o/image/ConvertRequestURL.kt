package de.datlag.model.m3o.image

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Keep
data class ConvertRequestURL(
    @SerialName("url") val url: String,
    @SerialName("name") val name: String = url.substringAfterLast('/'),
    @SerialName("outputURL") val outputURL: Boolean = false,
) : Parcelable
