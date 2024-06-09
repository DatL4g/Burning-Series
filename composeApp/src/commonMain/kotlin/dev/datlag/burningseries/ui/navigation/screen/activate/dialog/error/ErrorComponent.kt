package dev.datlag.burningseries.ui.navigation.screen.activate.dialog.error

import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.skeo.Stream

interface ErrorComponent : DialogComponent {
    val stream: Stream?

    fun back()
    fun watch(stream: Stream)
}