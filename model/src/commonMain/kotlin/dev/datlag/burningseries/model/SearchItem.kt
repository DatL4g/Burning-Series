package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SearchItem(
    @SerialName("title") override val title: String,
    @SerialName("href") override val href: String,
    @SerialName("cover") override val coverHref: String? = null,
    @SerialName("genre") val genre: String? = null
) : SeriesData() {

    @Transient
    val isAnime: Boolean = genre?.contains("Anime", ignoreCase = true) == true
}