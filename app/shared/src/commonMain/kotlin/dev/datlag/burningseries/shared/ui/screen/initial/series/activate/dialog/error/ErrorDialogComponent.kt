package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.error

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.shared.LocalDI
import dev.datlag.skeo.Stream
import org.kodein.di.DI

class ErrorDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val stream: Stream?,
    private val onDismissed: () -> Unit,
    private val watchVideo: (Stream) -> Unit
) : ErrorComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        CompositionLocalProvider(
            LocalDI provides di
        ) {
            ErrorDialog(this)
        }
    }

    override fun dismiss() {
        onDismissed()
    }

    override fun watch(stream: Stream) {
        watchVideo(stream)
    }
}