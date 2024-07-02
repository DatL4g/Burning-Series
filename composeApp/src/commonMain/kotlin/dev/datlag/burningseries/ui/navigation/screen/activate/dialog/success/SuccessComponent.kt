package dev.datlag.burningseries.ui.navigation.screen.activate.dialog.success

import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection

interface SuccessComponent : DialogComponent {
    val stream: ImmutableCollection<DirectLink>

    fun back()
    fun watch(stream: ImmutableCollection<DirectLink>)
}