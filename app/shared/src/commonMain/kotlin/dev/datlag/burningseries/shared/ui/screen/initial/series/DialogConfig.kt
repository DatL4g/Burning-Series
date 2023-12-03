package dev.datlag.burningseries.shared.ui.screen.initial.series

import dev.datlag.burningseries.model.Series
import kotlinx.serialization.Serializable

@Serializable
sealed class DialogConfig {

    @Serializable
    data class Season(
        val selected: Series.Season,
        val seasons: List<Series.Season>
    ) : DialogConfig()

    @Serializable
    data class Language(
        val selected: Series.Language,
        val languages: List<Series.Language>
    ) : DialogConfig()

    @Serializable
    data class StreamUnavailable(
        val series: Series,
        val episode: Series.Episode
    ) : DialogConfig()

    @Serializable
    data class Activate(
        val series: Series,
        val episode: Series.Episode
    ) : DialogConfig()
}