package dev.datlag.burningseries.ui.navigation.screen.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.network.SaveStateMachine
import dev.datlag.burningseries.network.state.SaveAction
import dev.datlag.burningseries.network.state.SaveState
import dev.datlag.burningseries.ui.navigation.DialogComponent
import dev.datlag.burningseries.ui.navigation.screen.activate.dialog.error.ErrorDialogComponent
import dev.datlag.burningseries.ui.navigation.screen.activate.dialog.success.SuccessDialog
import dev.datlag.burningseries.ui.navigation.screen.activate.dialog.success.SuccessDialogComponent
import dev.datlag.skeo.Stream
import dev.datlag.tooling.compose.ioDispatcher
import dev.datlag.tooling.compose.withMainContext
import dev.datlag.tooling.decompose.ioScope
import dev.datlag.tooling.scopeCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val series: Series,
    override val episode: Series.Episode,
    private val onBack: () -> Unit,
    private val onWatch: (Series, Series.Episode, Stream) -> Unit
) : ActivateComponent, ComponentContext by componentContext {

    private val json by instance<Json>()
    private val savedData: MutableSet<String> = mutableSetOf()
    private val saveStateMachine by instance<SaveStateMachine>()

    override val saveState: StateFlow<SaveState> = saveStateMachine.state.flowOn(
        context = ioDispatcher()
    ).stateIn(
        scope = ioScope(),
        started = SharingStarted.WhileSubscribed(),
        initialValue = SaveState.None
    )

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer()
    ) { config, context ->
        when (config) {
            is DialogConfig.Success -> SuccessDialogComponent(
                componentContext = context,
                di = di,
                stream = config.stream,
                onDismiss = dialogNavigation::dismiss,
                onBack = {
                    dialogNavigation.dismiss {
                        back()
                    }
                },
                onWatch = { stream ->
                    dialogNavigation.dismiss {
                        onWatch(series, episode, stream)
                    }
                }
            )
            is DialogConfig.Error -> ErrorDialogComponent(
                componentContext = context,
                di = di,
                stream = config.stream,
                onDismiss = dialogNavigation::dismiss,
                onBack = {
                    dialogNavigation.dismiss {
                        back()
                    }
                },
                onWatch = { stream ->
                    dialogNavigation.dismiss {
                        onWatch(series, episode, stream)
                    }
                }
            )
        }
    }

    @Composable
    override fun render() {
        onRender {
            ActivateScreen(this)
        }
    }

    override fun back() {
        onBack()
    }

    override fun onScraped(data: String?) {
        val trimmed = data?.trim()?.ifBlank { null } ?: return
        if (!trimmed.equals("null", true) && !trimmed.equals("undefined", true)) {
            val converted = scopeCatching {
                json.decodeFromString<HosterScraping>(trimmed)
            }.getOrNull()

            if (converted != null) {
                if (!savedData.contains(converted.href)) {
                    launchIO {
                        saveStateMachine.dispatch(SaveAction.Save(converted))
                    }
                }

                savedData.add(converted.href)
            }
        }
    }

    override fun success(stream: Stream?) {
        launchIO {
            saveStateMachine.dispatch(SaveAction.Clear)
            withMainContext {
                dialogNavigation.activate(DialogConfig.Success(stream))
            }
        }
    }

    override fun error(stream: Stream?) {
        launchIO {
            saveStateMachine.dispatch(SaveAction.Clear)
            withMainContext {
                dialogNavigation.activate(DialogConfig.Error(stream))
            }
        }
    }
}