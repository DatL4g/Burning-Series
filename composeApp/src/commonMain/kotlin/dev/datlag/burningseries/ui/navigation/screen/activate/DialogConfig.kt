package dev.datlag.burningseries.ui.navigation.screen.activate

import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.serialization.Serializable

@Serializable
sealed interface DialogConfig {

    @Serializable
    data class Success(
        val series: Series?,
        val episode: Series.Episode?,
        val stream: ImmutableCollection<DirectLink>
    ) : DialogConfig

    @Serializable
    data class Error(
        val series: Series?,
        val episode: Series.Episode?,
        val stream: ImmutableCollection<DirectLink>
    ) : DialogConfig
}