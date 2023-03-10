package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonBaseCaptchaEntry(
    @SerialName("url") val url: String,
    @SerialName("embed") val embed: Boolean,
    @SerialName("broken") val broken: Boolean = false
)
