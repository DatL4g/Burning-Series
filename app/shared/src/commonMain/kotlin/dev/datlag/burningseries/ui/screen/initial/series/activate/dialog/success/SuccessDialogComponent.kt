package dev.datlag.burningseries.ui.screen.initial.series.activate.dialog.success

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Stream
import org.kodein.di.DI

class SuccessDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val stream: Stream?,
    private val onDismissed: () -> Unit,
    private val watchVideo: (Stream) -> Unit
) : SuccessComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        SuccessDialog(this)
    }

    override fun dismiss() {
        onDismissed()
    }

    override fun watch(stream: Stream) {
        watchVideo(stream)
        dismiss()
    }
}