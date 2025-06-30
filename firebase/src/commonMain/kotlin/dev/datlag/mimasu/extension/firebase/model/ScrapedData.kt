package dev.datlag.mimasu.extension.firebase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ScrapedData(
    @SerialName("href") val href: String,
    @SerialName("url") val url: String,
    @SerialName("embed") val embed: Boolean = false
) {

    @Transient
    internal val fireStore = FireStore(
        id = href,
        url = url
    )

    @Serializable
    internal data class FireStore(
        @SerialName("id") val id: String,
        @SerialName("url") val url: String
    )
}