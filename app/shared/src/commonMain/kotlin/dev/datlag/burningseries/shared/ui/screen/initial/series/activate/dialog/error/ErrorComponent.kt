package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.error

import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent

interface ErrorComponent : DialogComponent {
    val stream: Stream?
    fun watch(stream: Stream)
}