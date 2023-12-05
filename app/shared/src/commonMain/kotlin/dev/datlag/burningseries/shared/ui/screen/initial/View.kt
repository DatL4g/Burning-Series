package dev.datlag.burningseries.shared.ui.screen.initial

import kotlinx.serialization.Serializable

@Serializable
sealed class View {

    @Serializable
    data object Home : View()

    @Serializable
    data object Favorite : View()

    @Serializable
    data object Search : View()
}