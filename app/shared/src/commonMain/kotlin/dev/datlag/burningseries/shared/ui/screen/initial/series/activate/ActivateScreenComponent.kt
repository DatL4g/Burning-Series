package dev.datlag.burningseries.shared.ui.screen.initial.series.activate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.model.HosterScraping
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.common.collectSafe
import dev.datlag.burningseries.model.common.scopeCatching
import dev.datlag.burningseries.model.state.SaveAction
import dev.datlag.burningseries.model.state.SaveState
import dev.datlag.burningseries.network.state.SaveStateMachine
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.burningseries.shared.common.ioDispatcher
import dev.datlag.burningseries.shared.common.ioScope
import dev.datlag.burningseries.shared.common.launchIO
import dev.datlag.burningseries.shared.common.withMainContext
import dev.datlag.burningseries.shared.ui.navigation.DialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.activate.component.DialogConfig
import dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.error.ErrorDialogComponent
import dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.success.SuccessDialogComponent
import dev.datlag.skeo.Stream
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val onDeviceReachable: Boolean,
    override val episode: Series.Episode,
    private val onGoBack: () -> Unit,
    private val watchVideo: (Stream) -> Unit
) : ActivateComponent, ComponentContext by componentContext {

    private val saveStateMachine by di.instance<SaveStateMachine>()
    private val saveState = saveStateMachine.state.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), SaveState.Waiting)
    private val json by di.instance<Json>()
    private val savedData: MutableSet<String> = mutableSetOf()

    override val isSaving = saveState.map { it is SaveState.Saving }.flowOn(ioDispatcher()).stateIn(ioScope(), SharingStarted.WhileSubscribed(), saveState.value is SaveState.Saving)

    private val dialogNavigation = SlotNavigation<DialogConfig>()
    override val dialog = childSlot(
        source = dialogNavigation,
        serializer = DialogConfig.serializer()
    ) { config, slotContext ->
        when (config) {
            is DialogConfig.Success -> SuccessDialogComponent(
                componentContext = slotContext,
                di = di,
                stream = config.stream,
                onDismissed = dialogNavigation::dismiss,
                watchVideo = { watchVideo(it) }
            ) as DialogComponent
            is DialogConfig.Error -> ErrorDialogComponent(
                componentContext = slotContext,
                di = di,
                stream = config.stream,
                onDismissed = dialogNavigation::dismiss,
                watchVideo = { watchVideo(it) }
            )
        }
    }

    private val backCallback = BackCallback {
        onGoBack()
    }

    init {
        backHandler.register(backCallback)

        ioScope().launchIO {
            saveState.collectSafe { state ->
                if (state is SaveState.Success) {
                    withMainContext {
                        dialogNavigation.activate(DialogConfig.Success(state.stream))
                    }
                } else if (state is SaveState.Error) {
                    withMainContext {
                        dialogNavigation.activate(DialogConfig.Error(state.stream))
                    }
                }
            }
        }
    }

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            ActivateScreen(this)
        }
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