package dev.datlag.burningseries.ui.screen.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.overlay.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.essenty.statekeeper.consume
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.network.repository.EpisodeRepository
import dev.datlag.burningseries.network.repository.SeriesRepository
import dev.datlag.burningseries.other.DefaultValue
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.dialog.language.LanguageDialogComponent
import dev.datlag.burningseries.ui.dialog.nostream.NoStreamDialogComponent
import dev.datlag.burningseries.ui.dialog.season.SeasonDialogComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class SeriesScreenComponent(
    componentContext: ComponentContext,
    private val href: String,
    override val initialInfo: SeriesInitialInfo,
    private val isEpisode: Boolean,
    override val onGoBack: () -> Unit,
    override val onEpisodeClicked: (Series, Series.Episode, List<VideoStream>) -> Unit,
    private val onActivateClicked: (Series, Series.Episode) -> Unit,
    override val di: DI
) : SeriesComponent, ComponentContext by componentContext {

    private val dialogNavigation = OverlayNavigation<DialogConfig>()
    private val _dialog = childOverlay(
        source = dialogNavigation,
        handleBackButton = true
    ) { config, componentContext ->
        when (config) {
            is DialogConfig.Language -> LanguageDialogComponent(
                componentContext,
                config.languages,
                config.selectedLanguage,
                onDismissed = dialogNavigation::dismiss,
                onSelected = ::onLanguageSelected,
                di = di
            ) as DialogComponent
            is DialogConfig.Season -> SeasonDialogComponent(
                componentContext,
                config.seasons,
                config.selectedSeason,
                onDismissed = dialogNavigation::dismiss,
                onSelected = ::onSeasonSelected,
                di = di
            ) as DialogComponent
            is DialogConfig.NoStream -> NoStreamDialogComponent(
                componentContext,
                config.episode,
                onDismissed = dialogNavigation::dismiss,
                onActivate = ::onActivate,
                di = di
            )
        }
    }
    override val dialog: Value<ChildOverlay<DialogConfig, DialogComponent>> = _dialog

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val seriesRepo: SeriesRepository by di.instance()
    override val title: Flow<String?> = seriesRepo.series.map { it?.title }
    override val cover: Flow<Cover?> = seriesRepo.series.map { it?.cover }
    override val selectedLanguage: Flow<String?> = seriesRepo.series.map { it?.selectedLanguage }
    override val languages: Flow<List<Series.Language>?> = seriesRepo.series.map { it?.languages }
    override val selectedSeason: Flow<Series.Season?> = seriesRepo.series.map { it?.currentSeason() }
    override val seasonText: Flow<String?> = seriesRepo.series.map { it?.season ?: selectedSeason.firstOrNull()?.title }
    override val seasons: Flow<List<Series.Season>?> = seriesRepo.series.map { it?.seasons }
    override val description: Flow<String?> = seriesRepo.series.map { it?.description }

    private val seriesInfo = seriesRepo.series.map { it?.infos }
    override val genreInfo: Flow<Series.Info?> = seriesInfo.map { it?.firstOrNull { info ->
        info.isGenre()
    } }
    override val additionalInfo: Flow<List<Series.Info>?> = seriesInfo.map { it?.filterNot { info ->
        info.isGenre()
    } }
    override val episodes: Flow<List<Series.Episode>> = seriesRepo.series.map { it?.episodes ?: emptyList() }

    private val episodeRepo: EpisodeRepository by di.instance()

    private var state: State = stateKeeper.consume(key = STATE_KEY) ?: State()
    private var loadedWantedEpisode: Boolean
        get() = state.loadedEpisode
        set(value) {
            state = state.copy(loadedEpisode = value)
        }

    override val linkedSeries: Flow<List<Series.Linked>> = seriesRepo.series.map { it?.linkedSeries ?: emptyList() }

    private val db: BurningSeriesDB by di.instance()
    private val imageDir: File by di.instance("ImageDir")

    private val dbSeries = seriesRepo.series.map {
        (it?.href ?: href).buildTitleHref()
    }.transform {
        return@transform emitAll(db.burningSeriesQueries.selectByHref(it).asFlow().mapToOneOrNull(Dispatchers.IO))
    }.flowOn(Dispatchers.IO)

    override val isFavorite = dbSeries.map { (it?.favoriteSince ?: 0) > 0 }

    init {
        scope.launch(Dispatchers.IO) {
            seriesRepo.loadFromHref(href)
        }
        if (isEpisode) {
            scope.launch(Dispatchers.IO) {
                episodes.collect { list ->
                    if (!loadedWantedEpisode) {
                        val wantedEpisode = list.find { it.href.equals(href, true) }
                        wantedEpisode?.let {
                            loadedWantedEpisode = true
                            loadEpisode(it)
                        }
                    }
                }
            }
        }
        stateKeeper.register(key = STATE_KEY) { state }
    }

    fun onLanguageSelected(language: Series.Language) {
        dialogNavigation.dismiss()
        scope.launch(Dispatchers.IO) {
            seriesRepo.series.value?.let { series ->
                seriesRepo.loadFromHref(series.hrefBuilder(series.currentSeason()?.value, language.value))
            }
        }
    }

    fun onSeasonSelected(season: Series.Season) {
        dialogNavigation.dismiss()
        scope.launch(Dispatchers.IO) {
            seriesRepo.series.value?.let { series ->
                seriesRepo.loadFromHref(series.hrefBuilder(season.value))
            }
        }
    }

    fun onActivate(episode: Series.Episode) {
        onActivateClicked(seriesRepo.series.value!!, episode)
    }

    override fun showDialog(config: DialogConfig) {
        dialogNavigation.activate(config)
    }

    override fun loadEpisode(episode: Series.Episode) {
        scope.launch(Dispatchers.IO) {
            episodeRepo.loadHosterStreams(episode)
            val episodeData = episodeRepo.streams.first()
            if (episodeData.isEmpty()) {
                withContext(CommonDispatcher.Main) {
                    showDialog(DialogConfig.NoStream(episode))
                }
            } else {
                withContext(CommonDispatcher.Main) {
                    val sortedList = episodeData.sortedByDescending { if(it.hoster.hoster.contains("VOE", true)) 1 else 0 }
                    onEpisodeClicked(seriesRepo.series.value!!, episode, sortedList) // ToDo("sort by settings")
                }
            }
        }
    }

    override fun toggleFavorite() {
        scope.launch(Dispatchers.IO) {
            val href = (seriesRepo.series.value?.href ?: this@SeriesScreenComponent.href).buildTitleHref()
            val coverBase64 = (seriesRepo.series.value?.cover?.base64 ?: initialInfo.cover?.base64)?.ifEmpty { null }
            val normalizedHref = href.fileName()
            val coverFile = File(imageDir, "$normalizedHref.bs")

            if (isFavorite.first()) {
                db.burningSeriesQueries.delete(href)
                try {
                    coverFile.delete()
                } catch (ignored: Throwable) { }
            } else {
                if (!coverBase64.isNullOrEmpty()) {
                    try {
                        coverFile.writeText(coverBase64)
                    } catch (ignored: Throwable) { }
                }
                db.burningSeriesQueries.insert(
                    href,
                    seriesRepo.series.value?.title ?: initialInfo.title,
                    (seriesRepo.series.value?.cover?.href ?: String()).ifEmpty {
                        initialInfo.cover?.href
                    },
                    Clock.System.now().epochSeconds
                )
            }
        }
    }

    @Composable
    override fun render() {
        SeriesScreen(this)
    }

    @Parcelize
    private data class State(val loadedEpisode: Boolean = false) : Parcelable

    companion object {
        private const val STATE_KEY = "STATE_KEY"
    }
}