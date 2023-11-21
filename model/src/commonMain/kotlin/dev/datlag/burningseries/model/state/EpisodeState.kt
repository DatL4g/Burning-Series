package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream

sealed interface EpisodeState {
    data object Waiting : EpisodeState
    data class Loading(override val episode: Series.Episode) : EpisodeState, EpisodeHolder
    data class SuccessHoster(override val episode: Series.Episode, val results: Collection<String>) : EpisodeState, EpisodeHolder
    data class SuccessStream(override val episode: Series.Episode, val results: Collection<Stream>) : EpisodeState, EpisodeHolder
    data class ErrorHoster(override val episode: Series.Episode) : EpisodeState, EpisodeHolder
    data class ErrorStream(override val episode: Series.Episode) : EpisodeState, EpisodeHolder

    interface EpisodeHolder {
        val episode: Series.Episode
    }
}

sealed interface EpisodeAction {
    data class Load(val episode: Series.Episode) : EpisodeAction
}