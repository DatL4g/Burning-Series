package dev.datlag.burningseries.ui.navigation.screen.video

import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VideoComponent : Component {

    val series: Series
    val episode: Series.Episode
    val streams: ImmutableCollection<DirectLink>
    val startingPos: Long
    val startingLength: Long

    val nextEpisode: Flow<EpisodeState>

    fun back()
    fun length(value: Long)
    fun progress(value: Long)
    fun ended()
    fun next(episode: Series.Episode, streams: ImmutableCollection<DirectLink>)
}