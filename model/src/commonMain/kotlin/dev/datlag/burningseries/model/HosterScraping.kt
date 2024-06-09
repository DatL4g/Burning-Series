package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HosterScraping(
    @SerialName("href") val href: String,
    @SerialName("url") val url: String,
    @SerialName("embed") val embed: Boolean = false
) {

    val fireStore = FireStore(
        id = href,
        url = url
    )

    @Serializable
    data class FireStore(
        @SerialName("id") val id: String,
        @SerialName("url") val url: String
    )
}
