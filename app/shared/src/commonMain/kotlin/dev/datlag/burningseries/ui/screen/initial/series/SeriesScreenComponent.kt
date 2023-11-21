package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import dev.datlag.burningseries.model.state.EpisodeAction
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.SeriesStateMachine
import dev.datlag.burningseries.ui.navigation.Component
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.screen.initial.series.activate.ActivateScreenComponent
import dev.datlag.burningseries.ui.screen.initial.series.dialog.language.LanguageDialogComponent
import dev.datlag.burningseries.ui.screen.initial.series.dialog.season.SeasonDialogComponent
import dev.datlag.burningseries.ui.screen.initial.series.dialog.unavailable.UnavailableDialog
import dev.datlag.burningseries.ui.screen.initial.series.dialog.unavailable.UnavailableDialogComponent
import io.ktor.client.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.instance

class SeriesScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val initialTitle: String,
    private val initialHref: String,
    private val initialCoverHref: String?,
    private val onGoBack: () -> Unit,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit
) : SeriesComponent, ComponentContext by componentContext {

    private val httpClient by di.instance<HttpClient>()
    private val seriesStateMachine = SeriesStateMachine(httpClient, initialHref)
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, SeriesState.Loading(initialHref))

    private val currentSeries = seriesState.mapNotNull { it as? SeriesState.Success }.map { it.series }.stateIn(ioScope(), SharingStarted.Lazily, null)
    override val title: StateFlow<String> = currentSeries.mapNotNull { it?.title }.stateIn(ioScope(), SharingStarted.Lazily, initialTitle)
    override val href: StateFlow<String> = currentSeries.mapNotNull { it?.href }.stateIn(ioScope(), SharingStarted.Lazily, BSUtil.fixSeriesHref(initialHref))
    override val commonHref: StateFlow<String> = href.map { BSUtil.commonSeriesHref(it) }.stateIn(ioScope(), SharingStarted.Lazily, BSUtil.commonSeriesHref(initialHref))
    override val coverHref: StateFlow<String?> = currentSeries.mapNotNull { it?.coverHref }.stateIn(ioScope(), SharingStarted.Lazily, initialCoverHref)

    private val database: BurningSeries by di.instance()
    override val isFavorite: StateFlow<Boolean> = commonHref.transform {
        return@transform emitAll(
            database
                .burningSeriesQueries
                .seriesByHref(it)
                .asFlow()
                .mapToOneOrNull(ioDispatcher())
                .map { s ->
                    s != null && s.favoriteSince > 0
                }
        )
    }.stateIn(ioScope(), SharingStarted.Lazily, database.burningSeriesQueries.seriesByHref(commonHref.value).executeAsOneOrNull()?.favoriteSince?.let { it > 0 } ?: false)

    private val episodeStateMachine by di.instance<EpisodeStateMachine>()
    private val episodeState: StateFlow<EpisodeState> = episodeStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, EpisodeState.Waiting)
    override val loadingEpisodeHref: StateFlow<String?> = episodeState.map {
        (it as? EpisodeState.Loading)?.episode?.href ?: (it as? EpisodeState.SuccessHoster)?.episode?.href
    }.stateIn(ioScope(), SharingStarted.Lazily, null)

    override val dbEpisodes = commonHref.transform {
        return@transform emitAll(database.burningSeriesQueries.selectEpisodesBySeriesHref(it).asFlow().mapToList(
            currentCoroutineContext()
        ))
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Lazily, database.burningSeriesQueries.selectEpisodesBySeriesHref(commonHref.value).executeAsList())

    private val navigation = SlotNavigation<SeriesConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        handleBackButton = false
    ) { config, context ->
        when (config) {
            is SeriesConfig.Activate -> ActivateScreenComponent(
                componentContext = context,
                di = di,
                episode = config.episode,
                onGoBack = navigation::dismiss,
                watchVideo = { watchVideo(commonHref.value, config.series, config.episode, listOf(it)) }
            )
        }
    }

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    private val _dialog = childSlot(
        key = "DialogChildSlot",
        source = dialogNavigation
    ) { config, slotContext ->
        when (config) {
            is DialogConfig.Season -> SeasonDialogComponent(
                componentContext = slotContext,
                di = di,
                defaultSeason = config.selected,
                seasons = config.seasons,
                onDismissed = dialogNavigation::dismiss,
                onSelected = {
                    loadNewSeason(it)
                }
            ) as DialogComponent
            is DialogConfig.Language -> LanguageDialogComponent(
                componentContext = slotContext,
                di = di,
                defaultLanguage = config.selected,
                languages = config.languages,
                onDismissed = dialogNavigation::dismiss,
                onSelected = {
                    loadNewLanguage(it)
                }
            )
            is DialogConfig.StreamUnavailable -> UnavailableDialogComponent(
                componentContext = slotContext,
                di = di,
                series = config.series,
                episode = config.episode,
                onDismissed = dialogNavigation::dismiss,
                onActivate = { series, episode ->
                    navigation.activate(SeriesConfig.Activate(series, episode))
                }
            )
        }
    }
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = _dialog

    private val backCallback = BackCallback {
        onGoBack()
    }

    init {
        backHandler.register(backCallback)

        ioScope().launchIO {
            episodeState.collect { state ->
                when (state) {
                    is EpisodeState.SuccessHoster -> {
                        saveSeriesAndEpisodeToDB()
                    }
                    is EpisodeState.SuccessStream -> {
                        val series = currentSeries.value ?: currentSeries.first() ?: currentSeries.value!!

                        withMainContext {
                            watchVideo(commonHref.value, series, state.episode, state.results)
                        }
                    }
                    is EpisodeState.ErrorHoster, is EpisodeState.ErrorStream -> {
                        val episode = (state as? EpisodeState.EpisodeHolder)?.episode
                        val series = currentSeries.value ?: currentSeries.first() ?: currentSeries.value!!

                        if (episode != null) {
                            withMainContext {
                                showDialog(
                                    DialogConfig.StreamUnavailable(series, episode)
                                )
                            }
                        }
                    }
                    else -> { }
                }
            }
        }
    }

    @Composable
    override fun render() {
        SeriesScreen(this)
    }

    override fun retryLoadingSeries(): Any? = ioScope().launchIO {
        seriesStateMachine.dispatch(SeriesAction.Retry)
    }

    override fun goBack() {
        onGoBack()
    }

    override fun showDialog(config: DialogConfig) {
        dialogNavigation.activate(config)
    }

    override fun toggleFavorite() = ioScope().launchIO {
        saveSeriesAndEpisodeToDB()

        database.burningSeriesQueries.updateSeriesFavoriteSince(
            since = if (isFavorite.value) 0L else Clock.System.now().epochSeconds,
            hrefPrimary = commonHref.value,
            href = href.value,
            title = title.value,
            coverHref = coverHref.value
        )
    }

    private suspend fun saveSeriesAndEpisodeToDB() {
        database.burningSeriesQueries.insertSeriesOrIgnore(
            hrefPrimary = commonHref.value,
            href = href.value,
            title = title.value,
            coverHref = coverHref.value,
            favoriteSince = if (isFavorite.value) 0L else Clock.System.now().epochSeconds
        )

        database.burningSeriesQueries.transaction {
            currentSeries.value?.episodes?.forEach { episode ->
                database.burningSeriesQueries.insertEpisode(
                    href = episode.href,
                    number = episode.number,
                    title = episode.title,
                    length = 0L,
                    progress = 0L,
                    seriesHref = commonHref.value
                )
            }
        }
    }

    override fun itemClicked(episode: Series.Episode): Any? = ioScope().launchIO {
        episodeStateMachine.dispatch(EpisodeAction.Load(episode))
    }

    private fun loadNewSeason(season: Series.Season) = ioScope().launchIO {
        (currentSeries.value ?: currentSeries.firstOrNull())?.let { series ->
            seriesStateMachine.dispatch(SeriesAction.Load(series.hrefBuilder(season.value)))
        }
    }

    private fun loadNewLanguage(language: Series.Language) = ioScope().launchIO {
        (currentSeries.value ?: currentSeries.firstOrNull())?.let { series ->
            seriesStateMachine.dispatch(SeriesAction.Load(series.hrefBuilder(language = language.value)))
        }
    }
}