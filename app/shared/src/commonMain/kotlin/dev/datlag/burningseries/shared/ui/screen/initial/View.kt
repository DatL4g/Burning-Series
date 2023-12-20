package dev.datlag.burningseries.shared.ui.screen.initial

import dev.datlag.burningseries.model.Shortcut
import kotlinx.serialization.Serializable

@Serializable
sealed class View {

    @Serializable
    data class Home(
        val shortcutIntent: Shortcut.Intent
    ) : View()

    @Serializable
    data object Favorite : View()

    @Serializable
    data object Search : View()
}