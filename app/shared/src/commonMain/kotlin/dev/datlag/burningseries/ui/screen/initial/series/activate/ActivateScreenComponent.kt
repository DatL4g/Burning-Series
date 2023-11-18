package dev.datlag.burningseries.ui.screen.initial.series.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.model.Series
import org.kodein.di.DI
import dev.datlag.burningseries.SharedRes
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.common.scopeCatching
import dev.datlag.burningseries.model.state.EpisodeAction
import dev.datlag.burningseries.model.state.EpisodeState
import dev.datlag.burningseries.model.state.SaveAction
import dev.datlag.burningseries.model.state.SaveState
import dev.datlag.burningseries.network.state.EpisodeStateMachine
import dev.datlag.burningseries.network.state.SaveStateMachine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.kodein.di.instance

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val episode: Series.Episode,
    private val onGoBack: () -> Unit
) : ActivateComponent, ComponentContext by componentContext {

    override val scrapingJs: String = SharedRes.assets.scrape_hoster.readText()
    private val saveStateMachine by di.instance<SaveStateMachine>()
    private val saveState = saveStateMachine.state.stateIn(ioScope(), SharingStarted.Lazily, SaveState.Waiting)
    private val json by di.instance<Json>()
    private val savedData: MutableSet<String> = mutableSetOf()

    private val backCallback = BackCallback {
        onGoBack()
    }

    init {
        backHandler.register(backCallback)

        ioScope().launchIO {
            saveState.collect { state ->
                when (val current = state) {
                    is SaveState.Saving -> {
                        println("Saving data: ${current.data}")
                    }
                    else -> { }
                }
            }
        }
    }

    @Composable
    override fun render() {
        ActivateScreen(this)
    }

    override fun back() {
        onGoBack()
    }

    override fun onScraped(data: String) {
        val trimmed = data.trim()
        if (trimmed.isNotBlank() && !trimmed.equals("null", true) && !trimmed.equals("undefined", true)) {
            val converted = scopeCatching {
                json.decodeFromString<HosterScraping>(trimmed)
            }.getOrNull()

            if (converted != null) {
                if (!savedData.contains(converted.href)) {
                    ioScope().launchIO {
                        saveStateMachine.dispatch(SaveAction.Save(converted))
                    }
                }

                savedData.add(converted.href)
            }
        }
    }
}