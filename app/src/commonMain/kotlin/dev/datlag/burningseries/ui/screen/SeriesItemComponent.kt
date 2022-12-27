package dev.datlag.burningseries.ui.screen

import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.navigation.Component
import java.io.File

interface SeriesItemComponent : Component {

    val imageDir: File

    fun onSeriesClicked(
        href: String,
        initialInfo: SeriesInitialInfo
    )
}