package dev.datlag.burningseries.shared.ui.navigation

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Shortcut
import dev.datlag.skeo.Stream
import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenConfig {

    @Serializable
    data class Home(
        val shortcutIntent: Shortcut.Intent
    ) : ScreenConfig()

    @Serializable
    data class Video(
        val schemeKey: String,
        val series: Series,
        val episode: Series.Episode,
        val streams: List<Stream>
    ) : ScreenConfig()
}