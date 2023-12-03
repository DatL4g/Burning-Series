package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.success

import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import dev.datlag.skeo.Stream

interface SuccessComponent : DialogComponent {
    val stream: Stream?
    fun watch(stream: Stream)
}