package dev.datlag.burningseries.ui.screen.video

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream
import org.kodein.di.DI

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val initialStreams: List<Stream>,
    private val onBack: () -> Unit
) : VideoComponent, ComponentContext by componentContext {

    override val streams: List<Stream> = initialStreams.sortedBy { it.headers.size }

    @Composable
    override fun render() {
        VideoScreen(this)
    }

    override fun back() {
        onBack()
    }
}