package dev.datlag.burningseries.ui.navigation

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
        val seriesData: SeriesData
    ) : RootConfig()
}