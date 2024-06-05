package dev.datlag.burningseries.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchItem(
    @SerialName("title") override val title: String,
    @SerialName("href") override val href: String,
    @SerialName("cover") override val coverHref: String? = null
) : SeriesData()
