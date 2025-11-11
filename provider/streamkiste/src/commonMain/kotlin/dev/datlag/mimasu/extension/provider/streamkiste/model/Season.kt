package dev.datlag.mimasu.extension.provider.streamkiste.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Season(
    @SerialName("_id") val id: String,
    @SerialName("s") val season: Int
)
