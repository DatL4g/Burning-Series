package dev.datlag.burningseries.ui.screen.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.overlay.*
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.common.CommonDispatcher
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.model.SaveInfo
import dev.datlag.burningseries.model.ScrapedHoster
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.VideoStream
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.repository.SaveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.instance
import dev.datlag.burningseries.ui.dialog.DialogComponent
import dev.datlag.burningseries.ui.dialog.save.SaveResultDialogComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val series: Series,
    override val episode: Series.Episode,
    override val onGoBack: () -> Unit,
    private val onWatch: (Series, Series.Episode, VideoStream) -> Unit,
    override val di: DI
) : ActivateComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(CommonDispatcher.Main + SupervisorJob())
    private val dialogNavigation = OverlayNavigation<DialogConfig>()
    private val _dialog = childOverlay(
        source = dialogNavigation,
        handleBackButton = true
    ) { config, componentContext ->
        when (config) {
            is DialogConfig.SaveResult -> SaveResultDialogComponent(
                componentContext,
                series,
                episode,
                config.success,
                config.stream,
                config.scrapedEpisodeHref,
                onWatch = onWatch,
                onBack = onGoBack,
                onDismissed = dialogNavigation::dismiss,
                di = di
            ) as DialogComponent
        }
    }
    override val dialog: Value<ChildOverlay<DialogConfig, DialogComponent>> = _dialog

    private val json: Json by di.instance()
    private val saveRepo: SaveRepository by di.instance()
    override val client: OkHttpClient by di.instance()

    private val scrapedList: MutableList<ScrapedHoster> = mutableListOf()

    override val saveInfo: Flow<SaveInfo> = saveRepo.saveInfo
    override val status: MutableStateFlow<Status> = MutableStateFlow(Status.LOADING)

    init {
        scope.launch(Dispatchers.IO) {
            saveInfo.collect { info ->
                withContext(CommonDispatcher.Main) {
                    dialogNavigation.activate(DialogConfig.SaveResult(
                        info.success,
                        info.stream,
                        info.href
                    ))
                }
            }
        }
    }

    override fun saveScrapedData(result: String) {
        val scraped = json.decodeFromString<ScrapedHoster>(result)
        if (!scrapedList.contains(scraped)) {
            scrapedList.add(scraped)
            scope.launch(Dispatchers.IO) {
                saveRepo.save(scraped)
            }
        }
    }

    override fun setStatus(status: Status) {
        scope.launch(Dispatchers.IO) {
            this@ActivateScreenComponent.status.emit(status)
        }
    }

    @Composable
    override fun render() {
        ActivateScreen(this)
    }
}