package dev.datlag.burningseries.ui.screen.initial.series.activate.dialog.success

import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.ui.navigation.DialogComponent

interface SuccessComponent : DialogComponent {

    val stream: Stream?
}