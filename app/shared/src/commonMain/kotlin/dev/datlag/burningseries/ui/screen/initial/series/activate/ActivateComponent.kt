package dev.datlag.burningseries.ui.screen.initial.series.activate

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.Component

interface ActivateComponent : Component {

    val episode: Series.Episode
    val scrapingJs: String

    fun back()
}