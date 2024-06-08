package dev.datlag.burningseries.ui.navigation.screen.medium

import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediumComponent : Component {
    val seriesData: SeriesData
    val initialIsAnime: Boolean

    val seriesState: StateFlow<SeriesState>
    val seriesTitle: Flow<String>
    val seriesSubTitle: Flow<String?>
    val seriesCover: Flow<String?>
    val seriesInfo: Flow<ImmutableCollection<Series.Info>>
    val seriesSeason: Flow<Series.Season?>
    val seriesSeasonList: Flow<ImmutableCollection<Series.Season>>
    val seriesLanguage: Flow<Series.Language?>
    val seriesLanguageList: Flow<ImmutableCollection<Series.Language>>
    val seriesDescription: Flow<String>
    val seriesIsAnime: Flow<Boolean>
    val episodes: Flow<ImmutableCollection<Series.Episode>>

    fun back()
    fun season(value: Series.Season)
    fun language(value: Series.Language)
}