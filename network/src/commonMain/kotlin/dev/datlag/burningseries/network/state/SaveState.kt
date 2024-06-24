package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.Stream

sealed interface SaveState {
    data object None : SaveState
    data class Saving(val episodeHref: String?, val data: HosterScraping) : SaveState
    data class Success(
        val series: Series?,
        val episode: Series.Episode?,
        val stream: Stream?
    ) : SaveState
    data class Error(
        val series: Series?,
        val episode: Series.Episode?,
        val stream: Stream?
    ) : SaveState
}

sealed interface SaveAction {
    data object Clear : SaveAction
    data class Save(val episodeHref: String?, val data: HosterScraping) : SaveAction
}