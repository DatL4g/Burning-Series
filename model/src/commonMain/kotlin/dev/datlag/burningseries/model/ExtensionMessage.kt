package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtensionMessage(
    @SerialName("set") val set: Boolean,
    @SerialName("href") val href: String,
    @SerialName("url") val url: String? = null
)