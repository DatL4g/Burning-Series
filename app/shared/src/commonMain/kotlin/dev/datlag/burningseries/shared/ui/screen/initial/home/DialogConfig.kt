package dev.datlag.burningseries.shared.ui.screen.initial.home

import kotlinx.serialization.Serializable

@Serializable
sealed class DialogConfig {

    @Serializable
    data object Sekret : DialogConfig()
}