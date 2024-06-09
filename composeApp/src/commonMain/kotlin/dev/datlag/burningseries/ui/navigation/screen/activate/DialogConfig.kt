package dev.datlag.burningseries.ui.navigation.screen.activate

import dev.datlag.skeo.Stream
import kotlinx.serialization.Serializable

@Serializable
sealed interface DialogConfig {

    @Serializable
    data class Success(val stream: Stream?) : DialogConfig

    @Serializable
    data class Error(val stream: Stream?) : DialogConfig
}