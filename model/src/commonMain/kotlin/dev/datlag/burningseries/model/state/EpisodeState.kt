package dev.datlag.burningseries.model.state

import dev.datlag.burningseries.model.Series

sealed interface EpisodeState {
    data object Waiting : EpisodeState
    data class Loading(val episode: Series.Episode) : EpisodeState
    data class SuccessHoster(val episode: Series.Episode, val results: Collection<String>) : EpisodeState
    data class SuccessStream(val results: Collection<String>) : EpisodeState
    data class Error(val msg: String) : EpisodeState
}

sealed interface EpisodeAction {
    data class Load(val episode: Series.Episode) : EpisodeAction
}