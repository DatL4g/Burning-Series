package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class VideoStream(
    @SerialName("hoster") val hoster: HosterStream,
    @SerialName("srcList") val srcList: List<String>,
    @SerialName("header") val header: Map<String, String>
) : Parcelable
