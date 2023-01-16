package dev.datlag.burningseries.ui.dialog.activate

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.ui.dialog.DialogComponent

interface ActivateComponent : DialogComponent {

    val episode: Series.Episode

    fun onConfirmActivate()
}