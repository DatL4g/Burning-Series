package dev.datlag.burningseries.ui.navigation.screen.video

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.skeo.Stream
import kotlinx.collections.immutable.ImmutableCollection

interface VideoComponent : Component {

    val episode: Series.Episode
    val streams: ImmutableCollection<Stream>

    fun back()
}