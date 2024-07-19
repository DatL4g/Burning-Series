package dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync

import dev.datlag.burningseries.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.StateFlow

interface SyncComponent : DialogComponent {
    val connectId: String
    val deviceNotFound: StateFlow<Boolean>
    val sendingTo: StateFlow<String?>
    val takingTime: StateFlow<Boolean>
    val connectionRefused: StateFlow<Boolean>
}