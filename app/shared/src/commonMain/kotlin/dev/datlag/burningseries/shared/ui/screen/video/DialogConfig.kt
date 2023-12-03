package dev.datlag.burningseries.shared.ui.screen.video

import kotlinx.serialization.Serializable

@Serializable
sealed class DialogConfig {

    @Serializable
    data class Subtitle(
        val list: List<VideoComponent.Subtitle>
    ) : DialogConfig()

    @Serializable
    data object Cast : DialogConfig()
}