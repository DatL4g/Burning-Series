package dev.datlag.burningseries.ui.screen.initial.series.dialog.unavailable

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.navigation.DialogComponent

interface UnavailableComponent : DialogComponent {

    val episode: Series.Episode

    fun activate()
}