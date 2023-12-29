package dev.datlag.burningseries.shared.ui.screen.initial.series

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SeriesComponent : Component {

    val seriesState: StateFlow<SeriesState>

    val child: Value<ChildSlot<*, Component>>
    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    val title: StateFlow<String>
    val href: StateFlow<String>
    val commonHref: StateFlow<String>
    val coverHref: StateFlow<String?>
    val isFavorite: StateFlow<Boolean>

    val loadingEpisodeHref: StateFlow<String?>

    val dbEpisodes: StateFlow<List<Episode>>
    val nextEpisodeToWatch: Flow<Series.Episode?>

    fun retryLoadingSeries(): Any?

    fun goBack()

    fun showDialog(config: DialogConfig)

    fun toggleFavorite(): Any?
    fun itemClicked(episode: Series.Episode): Any?
    fun itemLongClicked(episode: Series.Episode)
}