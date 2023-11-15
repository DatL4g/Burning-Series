package dev.datlag.burningseries.ui.screen.initial.series

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SeriesComponent : Component {

    val seriesState: StateFlow<SeriesState>
    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    val title: StateFlow<String>
    val href: StateFlow<String>
    val commonHref: StateFlow<String>
    val coverHref: StateFlow<String?>
    val isFavorite: StateFlow<Boolean>

    fun retryLoadingSeries(): Any?

    fun goBack()

    fun showDialog(config: DialogConfig)

    fun toggleFavorite(): Any?
    fun itemClicked(episode: Series.Episode): Any?
}