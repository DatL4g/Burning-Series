package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.activate

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent

interface ActivateComponent : DialogComponent {
    val series: Series
    val episode: Series.Episode

    fun activate()
}