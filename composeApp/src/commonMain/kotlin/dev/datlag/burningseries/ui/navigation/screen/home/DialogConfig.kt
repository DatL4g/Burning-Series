package dev.datlag.burningseries.ui.navigation.screen.home

import kotlinx.serialization.Serializable

@Serializable
sealed class DialogConfig {

    @Serializable
    data object Settings : DialogConfig()
}