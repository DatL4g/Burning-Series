package dev.datlag.burningseries.shared.ui.navigation

import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.Stream
import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenConfig {

    @Serializable
    data object Home : ScreenConfig()

    @Serializable
    data class Video(
        val schemeKey: String,
        val series: Series,
        val episode: Series.Episode,
        val streams: List<Stream>
    ) : ScreenConfig()
}