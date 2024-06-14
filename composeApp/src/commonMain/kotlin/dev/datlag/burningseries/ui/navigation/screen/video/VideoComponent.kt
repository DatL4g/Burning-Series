package dev.datlag.burningseries.ui.navigation.screen.video

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.skeo.Stream
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.coroutines.flow.StateFlow

interface VideoComponent : Component {

    val episode: Series.Episode
    val streams: ImmutableCollection<Stream>
    val startingPos: Long

    fun back()
    fun length(value: Long)
    fun progress(value: Long)
    fun ended()
}