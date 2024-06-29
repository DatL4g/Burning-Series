package dev.datlag.burningseries.ui.navigation.screen.home.dialog.qrcode

import dev.datlag.burningseries.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.StateFlow

interface QrCodeComponent : DialogComponent {
    val identifier: String
    val syncedSettings: StateFlow<Boolean>
}