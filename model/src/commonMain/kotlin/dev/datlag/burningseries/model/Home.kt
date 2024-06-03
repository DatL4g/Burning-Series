package dev.datlag.burningseries.model

import dev.datlag.burningseries.model.serializer.SerializableImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Home(
    @SerialName("latestSeries") val series: SerializableImmutableSet<Series> = persistentSetOf()
) {

    @Serializable
    data class Series(
        @SerialName("title") override val title: String,
        @SerialName("href") override val href: String,
        @SerialName("coverHref") val coverHref: String? = null
    ) : SeriesData()
}
