package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class Home {

    @Serializable
    data class Series(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String,
        @SerialName("isNsfw") val isNsfw: Boolean = false
    )
}