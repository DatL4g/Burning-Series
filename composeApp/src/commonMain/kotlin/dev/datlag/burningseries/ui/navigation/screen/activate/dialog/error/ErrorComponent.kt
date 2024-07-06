package dev.datlag.burningseries.ui.navigation.screen.activate.dialog.error

import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection

interface ErrorComponent : DialogComponent {
    val stream: ImmutableCollection<DirectLink>

    fun back()
    fun watch(stream: ImmutableCollection<DirectLink>)
}