package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.sponsor

import dev.datlag.burningseries.ui.navigation.DialogComponent

interface SponsorComponent : DialogComponent {
    val isLoggedIn: Boolean

    fun login()
}