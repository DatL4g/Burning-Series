package dev.datlag.burningseries.ui.navigation.screen.activate.dialog.success

import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.skeo.Stream

interface SuccessComponent : DialogComponent {
    val stream: Stream?

    fun back()
    fun watch(stream: Stream)
}