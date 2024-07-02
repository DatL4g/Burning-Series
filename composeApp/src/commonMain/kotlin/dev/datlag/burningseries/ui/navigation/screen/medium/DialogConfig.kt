package dev.datlag.burningseries.ui.navigation.screen.medium

import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.DirectLink
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.serialization.Serializable

@Serializable
sealed interface DialogConfig {

    @Serializable
    data class Activate(
        val series: Series,
        val episode: Series.Episode
    ) : DialogConfig

    @Serializable
    data class Sponsor(
        val series: Series,
        val episode: Series.Episode,
        val streams: ImmutableCollection<DirectLink>
    ) : DialogConfig
}