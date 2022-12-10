package dev.datlag.burningseries.ui.screen.series

import com.arkivanov.decompose.router.overlay.ChildOverlay
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.other.DefaultValue
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface SeriesComponent : Component {

    val dialog: Value<ChildOverlay<DialogConfig, DialogComponent>>

    val initialInfo: SeriesInitialInfo
    val onGoBack: () -> Unit

    val title: Flow<String?>
    val cover: Flow<Cover?>
    val selectedLanguage: Flow<String?>
    val languages: Flow<List<Series.Language>?>
    val selectedSeason: Flow<Series.Season?>
    val seasonText: Flow<String?>
    val seasons: Flow<List<Series.Season>?>
    val description: Flow<String?>

    val episodes: Flow<List<Series.Episode>>

    fun showDialog()
}