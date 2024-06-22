package dev.datlag.burningseries.ui.navigation.screen.medium

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.database.CombinedEpisode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.skeo.Stream
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
    val combinedEpisodes: Flow<ImmutableCollection<CombinedEpisode>>

    val episodeState: StateFlow<EpisodeState>

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>>

    val isFavorite: StateFlow<Boolean>

    fun back()
    fun season(value: Series.Season)
    fun language(value: Series.Language)
    fun episode(episode: Series.Episode)
    fun episode(combinedEpisode: CombinedEpisode) = episode(combinedEpisode.default)
    fun watched(series: Series, episode: Series.Episode)
    fun watched(series: Series, combinedEpisode: CombinedEpisode) = watched(series, combinedEpisode.default)
    fun unwatched(series: Series, episode: Series.Episode)
    fun unwatched(series: Series, combinedEpisode: CombinedEpisode) = unwatched(series, combinedEpisode.default)
    fun watch(
        series: Series,
        episode: Series.Episode,
        streams: ImmutableCollection<Stream>
    )
    fun activate(series: Series, episode: Series.Episode)
    fun activate(series: Series, episode: CombinedEpisode) = activate(series, episode.default)
    fun setFavorite(series: Series)
    fun unsetFavorite(series: Series)
}