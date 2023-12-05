package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.unavailable

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent

interface UnavailableComponent : DialogComponent {

    val series: Series
    val episode: Series.Episode

    fun activate()
}