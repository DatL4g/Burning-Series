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
import dev.datlag.burningseries.network.repository.SeriesRepository
import dev.datlag.burningseries.other.DefaultValue
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.dialog.language.LanguageDialogComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class SeriesScreenComponent(
    componentContext: ComponentContext,
    private val href: String,
    override val initialInfo: SeriesInitialInfo,
    override val onGoBack: () -> Unit,
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
                onDismissed = dialogNavigation::dismiss,
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

    override val episodes: Flow<List<Series.Episode>> = seriesRepo.series.map { it?.episodes ?: emptyList() }

    init {
        scope.launch(Dispatchers.IO) {
            seriesRepo.loadFromHref(href)
        }
    }

    override fun showDialog() {
        dialogNavigation.activate(DialogConfig.Language)
    }

    @Composable
    override fun render() {
        SeriesScreen(this)
    }
}