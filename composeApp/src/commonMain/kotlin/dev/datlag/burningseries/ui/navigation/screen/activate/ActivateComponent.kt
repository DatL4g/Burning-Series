package dev.datlag.burningseries.ui.navigation.screen.activate

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.state.SaveState
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.flow.StateFlow

interface ActivateComponent : Component {
    val episode: Series.Episode
    val saveState: StateFlow<SaveState>

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    fun back()
    fun onScraped(episodeHref: String?, data: String?)
    fun success(
        series: Series?,
        episode: Series.Episode?,
        stream: Stream?
    )
    fun error(
        series: Series?,
        episode: Series.Episode?,
        stream: Stream?
    )
}