package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WrapAPIResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: JsonElement
)
