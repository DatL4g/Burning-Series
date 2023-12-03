package dev.datlag.burningseries.shared.ui.screen.initial.series

import dev.datlag.burningseries.model.Series
import kotlinx.serialization.Serializable

@Serializable
sealed class SeriesConfig {

    @Serializable
    data class Activate(
        val series: Series,
        val episode: Series.Episode
    ) : SeriesConfig()
}