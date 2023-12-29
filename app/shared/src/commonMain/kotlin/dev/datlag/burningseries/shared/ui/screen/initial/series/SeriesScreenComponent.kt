package dev.datlag.burningseries.shared.ui.screen.initial.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.database.BurningSeries
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.common.collectSafe
import dev.datlag.burningseries.model.common.safeCast
import dev.datlag.burningseries.model.state.EpisodeAction
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.model.state.SeriesAction
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.network.WrapAPI
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.SeriesStateMachine
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.Sekret
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.common.withMainContext
import dev.datlag.burningseries.shared.getPackageName
import dev.datlag.burningseries.shared.other.Crashlytics
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.ui.navigation.Component
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.activate.ActivateScreenComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.activate.ActivateDialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.language.LanguageDialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.season.SeasonDialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.unavailable.UnavailableDialogComponent
import dev.datlag.skeo.Stream
import io.ktor.client.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.math.max

class SeriesScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val initialTitle: String?,
    private val initialHref: String,
    private val initialCoverHref: String?,
    private val onGoBack: () -> Unit,
    private val watchVideo: (String, Series, Series.Episode, Collection<Stream>) -> Unit
) : SeriesComponent, ComponentContext by componentContext {

    private val httpClient by di.instance<HttpClient>()
    private val json by di.instance<Json>()
    private val wrapAPI by di.instance<WrapAPI>()

    private val seriesStateMachine = SeriesStateMachine(
        client = httpClient,
        href = initialHref,
        json = json,
        wrapAPI = wrapAPI,
        wrapAPIKey = if (StateSaver.sekretLibraryLoaded) {
            Sekret().wrapApi(getPackageName())
        } else { null },
    )
    override val seriesState: StateFlow<SeriesState> = seriesStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), SeriesState.Loading(initialHref))

    private val successState = seriesState.mapNotNull { it.safeCast<SeriesState.Success>() }.flowOn(ioDispatcher())
    private val currentSeries = successState.map { it.series }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), null)
    private val onDeviceReachable = successState.map { it.onDeviceReachable }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.Eagerly, true)
    override val title: StateFlow<String> = currentSeries.mapNotNull { it?.bestTitle }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), initialTitle ?: String())
    override val href: StateFlow<String> = currentSeries.mapNotNull { it?.href }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), BSUtil.fixSeriesHref(initialHref))
    override val commonHref: StateFlow<String> = href.map {
        val commonized = BSUtil.commonSeriesHref(it)
        database.burningSeriesQueries.seriesUpdateHrefByCommonHref(it, commonized)
        commonized
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), BSUtil.commonSeriesHref(initialHref))
    override val coverHref: StateFlow<String?> = currentSeries.mapNotNull { it?.coverHref }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), initialCoverHref)

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
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), database.burningSeriesQueries.seriesByHref(commonHref.value).executeAsOneOrNull()?.favoriteSince?.let { it > 0 } ?: false)

    private val episodeStateMachine by di.instance<EpisodeStateMachine>()
    private val episodeState: StateFlow<EpisodeState> = episodeStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), EpisodeState.Waiting)
    override val loadingEpisodeHref: StateFlow<String?> = episodeState.map {
        it.safeCast<EpisodeState.Loading>()?.episode?.href ?: it.safeCast<EpisodeState.SuccessHoster>()?.episode?.href
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), null)

    override val dbEpisodes = commonHref.transform {
        return@transform emitAll(database.burningSeriesQueries.selectEpisodesBySeriesHref(it).asFlow().mapToList(
            currentCoroutineContext()
        ))
    }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), database.burningSeriesQueries.selectEpisodesBySeriesHref(commonHref.value).executeAsList())

    override val nextEpisodeToWatch = combine(currentSeries.map { it?.episodes }, dbEpisodes) { seriesEpisodes, savedEpisodes ->
        if (seriesEpisodes == null) {
            null
        } else {
            val maxWatchedEpisode = savedEpisodes.maxByOrNull {
                if (it.progress > 0L || it.progress == Long.MIN_VALUE) {
                    it.number.toIntOrNull() ?: -1
                } else {
                    -1
                }
            }

            if (maxWatchedEpisode == null) {
                seriesEpisodes.firstOrNull()
            } else {
                val length = max(maxWatchedEpisode.length, 0L)
                val progress = if (maxWatchedEpisode.progress == Long.MIN_VALUE) {
                    Long.MIN_VALUE
                } else {
                    max(maxWatchedEpisode.progress, 0L)
                }

                val isFinished = if (length > 0L && progress > 0L) {
                    (progress.toDouble() / length.toDouble() * 100.0).toFloat() >= 85F
                } else {
                    progress == Long.MIN_VALUE
                }

                val wantedNumber = if (isFinished) {
                    maxWatchedEpisode.number.toIntOrNull()?.plus(1)?.toString()
                } else {
                    maxWatchedEpisode.number
                }

                if (!wantedNumber.isNullOrBlank()) {
                    seriesEpisodes.firstOrNull {
                        it.number.equals(wantedNumber, true)
                    } ?: seriesEpisodes.firstOrNull {
                        it.number.toIntOrNull() == (wantedNumber.toIntOrNull() ?: return@firstOrNull false)
                    }
                } else {
                    null
                }
            }
        }
    }.flowOn(ioDispatcher())

    private val navigation = SlotNavigation<SeriesConfig>()
    override val child: Value<ChildSlot<*, Component>> = childSlot(
        source = navigation,
        serializer = SeriesConfig.serializer(),
        handleBackButton = false
    ) { config, context ->
        when (config) {
            is SeriesConfig.Activate -> ActivateScreenComponent(
                componentContext = context,
                di = di,
                onDeviceReachable = onDeviceReachable.value,
                episode = config.episode,
                onGoBack = navigation::dismiss,
                watchVideo = { watchVideo(commonHref.value, config.series, config.episode, listOf(it)) }
            )
        }
    }

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    private val _dialog = childSlot(
        key = "DialogChildSlot",
        source = dialogNavigation,
        serializer = DialogConfig.serializer()
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
            is DialogConfig.Activate -> ActivateDialogComponent(
                componentContext = slotContext,
                di = di,
                series = config.series,
                episode = config.episode,
                onDismiss = dialogNavigation::dismiss,
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
            episodeState.collectSafe { state ->
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
                        val episode = state.safeCast<EpisodeState.EpisodeHolder>()?.episode
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
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            SeriesScreen(this)
        }
        SideEffect {
            Crashlytics.screen(this)
        }
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

                episode.hosters.forEach { hoster ->
                    database.burningSeriesQueries.insertHoster(
                        href = hoster.href,
                        title = hoster.title,
                        episodeHref = episode.href
                    )
                }
            }
        }
    }

    override fun itemClicked(episode: Series.Episode): Any? = ioScope().launchIO {
        episodeStateMachine.dispatch(EpisodeAction.Load(episode))
    }

    override fun itemLongClicked(episode: Series.Episode) {
        currentSeries.value?.let { series ->
            showDialog(DialogConfig.Activate(
                series = series,
                episode = episode
            ))
        }
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