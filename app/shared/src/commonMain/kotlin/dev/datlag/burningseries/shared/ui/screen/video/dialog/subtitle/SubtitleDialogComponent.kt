package dev.datlag.burningseries.shared.ui.screen.video.dialog.subtitle

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.shared.ui.screen.video.VideoComponent
import org.kodein.di.DI

class SubtitleDialogComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val initialChosen: VideoComponent.Subtitle?,
    override val list: List<VideoComponent.Subtitle>,
    private val onDismiss: () -> Unit,
    private val onChosen: (VideoComponent.Subtitle?) -> Unit
) : SubtitleComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        SubtitleDialog(this)
    }

    override fun dismiss() {
        onDismiss()
    }

    override fun choose(target: VideoComponent.Subtitle?) {
        onChosen(target)
        dismiss()
    }
}