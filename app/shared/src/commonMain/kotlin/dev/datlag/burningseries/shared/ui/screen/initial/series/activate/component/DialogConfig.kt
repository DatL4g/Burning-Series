package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.component

import dev.datlag.skeo.Stream
import kotlinx.serialization.Serializable

@Serializable
sealed class DialogConfig {

    @Serializable
    data class Success(val stream: Stream?) : DialogConfig()

    @Serializable
    data class Error(val stream: Stream?) : DialogConfig()
}