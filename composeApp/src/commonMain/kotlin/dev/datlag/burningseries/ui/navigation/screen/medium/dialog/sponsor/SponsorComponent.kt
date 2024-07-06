package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.sponsor

import dev.datlag.burningseries.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SponsorComponent : DialogComponent {
    val isLoggedIn: Flow<Boolean>

    fun login()
}