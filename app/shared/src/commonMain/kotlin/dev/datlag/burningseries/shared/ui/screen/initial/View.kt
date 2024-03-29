package dev.datlag.burningseries.shared.ui.screen.initial

import dev.datlag.burningseries.model.Shortcut
import kotlinx.serialization.Serializable

@Serializable
sealed class View {

    @Serializable
    data object Sponsor : View()

    @Serializable
    data class Home(
        val shortcutIntent: Shortcut.Intent
    ) : View()

    @Serializable
    data object Favorite : View()
}