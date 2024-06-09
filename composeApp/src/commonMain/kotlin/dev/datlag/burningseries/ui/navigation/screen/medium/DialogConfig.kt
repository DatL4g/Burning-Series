package dev.datlag.burningseries.ui.navigation.screen.medium

import dev.datlag.burningseries.model.Series
import kotlinx.serialization.Serializable

@Serializable
sealed interface DialogConfig {

    @Serializable
    data class Activate(val episode: Series.Episode) : DialogConfig
}