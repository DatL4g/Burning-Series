package dev.datlag.burningseries.ui.navigation

import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.model.serializer.SerializableImmutableList
import dev.datlag.burningseries.model.serializer.SerializableImmutableSet
import dev.datlag.skeo.Stream
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.serialization.Serializable

@Serializable
sealed class RootConfig {

    @Serializable
    data object Welcome : RootConfig()

    @Serializable
    data object Home : RootConfig()

    @Serializable
    data class Medium(
        val seriesData: SeriesData,
        val isAnime: Boolean = if (seriesData is SearchItem) seriesData.isAnime else false
    ) : RootConfig()

    @Serializable
    data class Video(
        val episode: Series.Episode,
        val streams: SerializableImmutableSet<Stream>
    ) : RootConfig()

    @Serializable
    data class Activate(
        val episode: Series.Episode
    ) : RootConfig()
}