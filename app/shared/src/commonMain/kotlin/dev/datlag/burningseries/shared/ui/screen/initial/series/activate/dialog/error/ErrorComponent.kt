package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.error

import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import dev.datlag.skeo.Stream

interface ErrorComponent : DialogComponent {
    val stream: Stream?
    fun watch(stream: Stream)
}