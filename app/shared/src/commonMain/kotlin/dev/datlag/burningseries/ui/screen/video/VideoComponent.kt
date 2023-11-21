package dev.datlag.burningseries.ui.screen.video

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.StateFlow

interface VideoComponent : Component {

    val series: Series
    val episode: StateFlow<Series.Episode>
    val streams: List<Stream>

    val startingPos: StateFlow<Long>

    fun back()
    fun ended()
    fun lengthUpdate(millis: Long)
    fun progressUpdate(millis: Long)
}