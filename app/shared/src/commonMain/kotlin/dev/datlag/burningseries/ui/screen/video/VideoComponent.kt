package dev.datlag.burningseries.ui.screen.video

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.ui.navigation.Component

interface VideoComponent : Component {

    val streams: List<Stream>

    fun back()
}