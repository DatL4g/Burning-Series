package dev.datlag.burningseries.ui.navigation.screen.medium

import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.ui.navigation.Component

interface MediumComponent : Component {
    val initialSeriesData: SeriesData
}