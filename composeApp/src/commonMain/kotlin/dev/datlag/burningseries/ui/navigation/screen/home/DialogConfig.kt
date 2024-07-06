package dev.datlag.burningseries.ui.navigation.screen.home

import dev.datlag.burningseries.github.model.UserAndRelease
import kotlinx.serialization.Serializable

@Serializable
sealed class DialogConfig {

    @Serializable
    data object Settings : DialogConfig()

    @Serializable
    data class Release(val release: UserAndRelease.Release) : DialogConfig()

    @Serializable
    data object QrCode : DialogConfig()

    @Serializable
    data class Sync(val id: String) : DialogConfig()
}