package dev.datlag.burningseries.ui.screen.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.overlay.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.coroutineScope
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
import org.kodein.di.DI
import org.kodein.di.instance

class SeriesScreenComponent(
    componentContext: ComponentContext,
    private val href: String,
    override val initialInfo: SeriesInitialInfo,
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

    init {
        scope.launch(Dispatchers.IO) {
            seriesRepo.loadFromHref(href)
        }
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

    @Composable
    override fun render() {
        SeriesScreen(this)
    }
}