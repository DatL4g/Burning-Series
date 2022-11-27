package dev.datlag.burningseries.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cover(
    @SerialName("href") val href: String,
    @SerialName("base64") val base64: String = String(),
    @SerialName("blurHash") val blurHash: String = String()
)
