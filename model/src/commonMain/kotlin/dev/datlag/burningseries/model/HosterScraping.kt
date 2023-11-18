package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HosterScraping(
    @SerialName("href") val href: String,
    @SerialName("url") val url: String,
    @SerialName("embed") val embed: Boolean = false
) {
    val firestore = Firestore(
        id = href,
        url = url
    )

    val jsonBase = JsonBaseCaptchaEntry(
        url = url,
        embed = embed,
        broken = false
    )

    @Serializable
    data class Firestore(
        @SerialName("id") val id: String,
        @SerialName("url") val url: String
    )
}
