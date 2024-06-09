package dev.datlag.burningseries.network.state

import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.Stream
import kotlinx.collections.immutable.ImmutableCollection

sealed interface EpisodeState {
    data object None : EpisodeState

    sealed interface EpisodeHolder : EpisodeState {
        val episode: Series.Episode
    }

    data class Loading(override val episode: Series.Episode) : EpisodeHolder

    data class SuccessHoster(
        override val episode: Series.Episode,
        val results: ImmutableCollection<String>
    ): EpisodeHolder

    data class SuccessStream(
        override val episode: Series.Episode,
        val results: ImmutableCollection<Stream>
    ): EpisodeHolder

    data class ErrorHoster(override val episode: Series.Episode) : EpisodeHolder

    data class ErrorStream(override val episode: Series.Episode) : EpisodeHolder
}

sealed interface EpisodeAction {
    data object Clear : EpisodeAction
    data class Load(val episode: Series.Episode) : EpisodeAction
}