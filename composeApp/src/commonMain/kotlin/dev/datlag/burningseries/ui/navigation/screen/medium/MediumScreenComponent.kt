package dev.datlag.burningseries.ui.navigation.screen.medium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import dev.chrisbanes.haze.HazeState
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.database.CombinedEpisode
import dev.datlag.burningseries.database.common.combinedEpisodesForSeries
import dev.datlag.burningseries.database.common.episodeForSeries
import dev.datlag.burningseries.database.common.episodeRefreshingData
import dev.datlag.burningseries.database.common.insertEpisodeOrIgnore
import dev.datlag.burningseries.database.common.isFavorite
import dev.datlag.burningseries.database.common.isFavoriteOneShot
import dev.datlag.burningseries.database.common.setEpisodeUnwatched
import dev.datlag.burningseries.database.common.setEpisodeWatched
import dev.datlag.burningseries.database.common.setSeriesFavorite
import dev.datlag.burningseries.database.common.unsetSeriesFavorite
import dev.datlag.burningseries.database.common.updateSeriesData
import dev.datlag.burningseries.database.common.updateSeriesHref
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesData
import dev.datlag.burningseries.network.EpisodeStateMachine
import dev.datlag.burningseries.network.SeriesStateMachine
import dev.datlag.burningseries.network.state.EpisodeAction
import dev.datlag.burningseries.network.state.EpisodeState
import dev.datlag.burningseries.network.state.SeriesState
import dev.datlag.burningseries.other.UserHelper
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.navigation.screen.medium.dialog.activate.ActivateDialogComponent
import dev.datlag.burningseries.ui.navigation.screen.medium.dialog.sponsor.SponsorDialogComponent
import dev.datlag.skeo.Stream
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.compose.withIOContext
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.decompose.ioScope
import dev.datlag.tooling.safeCast
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableCollection
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.instance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest

class MediumScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val initialSeriesData: SeriesData,
    override val initialIsAnime: Boolean,
    private val initialLanguage: Language?,
    private val onBack: () -> Unit,
    private val onWatch: (Series, Series.Episode, ImmutableCollection<Stream>) -> Unit,
    private val onActivate: (Series, Series.Episode) -> Unit
) : MediumComponent, ComponentContext by componentContext {

    private val seriesStateMachine by instance<SeriesStateMachine>()
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = seriesStateMachine.currentState
    )

    private val successState = seriesState.mapNotNull {
        it.safeCast<SeriesState.Success>().also { success ->
            seriesData = success?.series ?: seriesData
        }
    }

    override var seriesData: SeriesData = initialSeriesData
        private set(value) {
            field = value

            database.updateSeriesData(value)
        }

    override val seriesTitle: Flow<String> = successState.map { it.series.mainTitle }
    override val seriesSubTitle: Flow<String?> = successState.map { it.series.subTitle }
    override val seriesCover: Flow<String?> = successState.map { it.series.coverHref ?: initialSeriesData.coverHref }
    override val seriesInfo: Flow<ImmutableCollection<Series.Info>> = successState.map { it.series.infoWithoutGenre }
    override val seriesSeason: Flow<Series.Season?> = successState.map { it.series.currentSeason }
    override val seriesSeasonList: Flow<ImmutableCollection<Series.Season>> = successState.map { it.series.seasons }
    override val seriesLanguage: Flow<Series.Language?> = successState.map { it.series.currentLanguage }
    override val seriesLanguageList: Flow<ImmutableCollection<Series.Language>> = successState.map { it.series.languages }
    override val seriesDescription: Flow<String> = successState.map { it.series.description }
    override val seriesIsAnime: Flow<Boolean> = successState.map { it.series.isAnime }
    private val episodes: Flow<ImmutableCollection<Series.Episode>> = successState.map { it.series.episodes }

    private val episodeStateMachine by instance<EpisodeStateMachine>()
    override val episodeState: StateFlow<EpisodeState> = episodeStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = EpisodeState.None
    )

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer()
    ) { config, context ->
        when (config) {
            is DialogConfig.Activate -> ActivateDialogComponent(
                componentContext = context,
                di = di,
                onDismiss = dialogNavigation::dismiss,
                onActivate = {
                    dialogNavigation.dismiss {
                        onActivate(
                            config.series,
                            config.episode
                        )
                    }
                }
            )
            is DialogConfig.Sponsor -> SponsorDialogComponent(
                componentContext = context,
                di = di,
                onDismiss = {
                    dialogNavigation.dismiss {
                        watch(
                            config.series,
                            config.episode,
                            config.streams
                        )
                    }
                }
            )
        }
    }

    private val database by instance<BurningSeries>()
    override val isFavorite: StateFlow<Boolean> = database.isFavorite(
        seriesData,
        ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = database.isFavoriteOneShot(seriesData)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val combinedEpisodes: Flow<ImmutableCollection<CombinedEpisode>> = episodes.transformLatest { collection ->
        emit(database.combinedEpisodesForSeries(collection, seriesData))

        val allFlows = collection.map { database.episodeRefreshingData(it, currentCoroutineContext()) }
        emitAll(
            combine(allFlows) { all ->
                all.toList().toImmutableSet()
            }
        )
    }

    private val userHelper by instance<UserHelper>()

    init {
        val hrefWithLanguage = if (initialLanguage != null) {
            seriesData.toHref(newLanguage = initialLanguage.code)
        } else {
            seriesData.toHref()
        }

        seriesStateMachine.href(hrefWithLanguage)
    }

    @Composable
    override fun render() {
        val haze = remember { HazeState() }

        CompositionLocalProvider(
            LocalHaze provides haze
        ) {
            onRenderWithScheme(initialSeriesData) {
                MediumScreen(this, it)
            }
        }
    }

    override fun back() {
        onBack()
    }

    override fun season(value: Series.Season) {
        seriesStateMachine.href(seriesData.toHref(newSeason = value.value))
    }

    override fun language(value: Series.Language) {
        seriesStateMachine.href(seriesData.toHref(newLanguage = value.value))
    }

    override fun episode(episode: Series.Episode) {
        launchIO {
            episodeStateMachine.dispatch(EpisodeAction.Load(episode))
        }
    }

    private fun watch(
        series: Series,
        episode: Series.Episode,
        streams: ImmutableCollection<Stream>
    ) {
        launchIO {
            episodeStateMachine.dispatch(EpisodeAction.Clear)
            withMainContext {
                onWatch(series, episode, streams)
            }
        }
    }

    override fun activate(series: Series, episode: Series.Episode) {
        launchIO {
            episodeStateMachine.dispatch(EpisodeAction.Clear)
            withMainContext {
                dialogNavigation.activate(DialogConfig.Activate(series, episode))
            }
        }
    }

    override fun setFavorite(series: Series) {
        database.setSeriesFavorite(series)
    }

    override fun unsetFavorite(series: Series) {
        database.unsetSeriesFavorite(series)
    }

    override fun watched(series: Series, episode: Series.Episode) {
        database.insertEpisodeOrIgnore(episode, series)
        database.setEpisodeWatched(episode)
    }

    override fun unwatched(series: Series, episode: Series.Episode) {
        database.insertEpisodeOrIgnore(episode, series)
        database.setEpisodeUnwatched(episode)
    }

    override fun showSponsoringOrWatch(
        series: Series,
        episode: Series.Episode,
        streams: ImmutableCollection<Stream>
    ) {
        val isSponsor = userHelper.isSponsoring.value
        if (isSponsor) {
            watch(series, episode, streams)
        } else {
            launchMain {
                if (withIOContext { userHelper.requiresSponsoring() }) {
                    episodeStateMachine.dispatch(EpisodeAction.Clear)
                    dialogNavigation.activate(
                        DialogConfig.Sponsor(
                            series, episode, streams
                        )
                    )
                } else {
                    watch(series, episode, streams)
                }
            }
        }
    }
}