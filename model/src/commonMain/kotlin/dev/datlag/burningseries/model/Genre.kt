package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    @SerialName("title") val title: String,
    @SerialName("items") val items: List<Item>
) {

    @Serializable
    data class Item(
        @SerialName("title") val title: String,
        @SerialName("href") val href: String
    )
}
