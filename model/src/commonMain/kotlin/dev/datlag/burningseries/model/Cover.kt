package dev.datlag.burningseries.model

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Cover(
    @SerialName("href") val href: String,
    @SerialName("base64") val base64: String = String(),
    @SerialName("blurHash") val blurHash: String = String()
) : Parcelable