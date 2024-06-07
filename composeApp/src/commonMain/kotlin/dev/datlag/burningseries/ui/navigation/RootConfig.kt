package dev.datlag.burningseries.ui.navigation

import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.model.SeriesData
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
}