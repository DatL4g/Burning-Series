package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import dev.datlag.skeo.Stream
import kotlinx.collections.immutable.ImmutableCollection
import org.kodein.di.DI

class VideoScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val episode: Series.Episode,
    override val streams: ImmutableCollection<Stream>,
    private val onBack: () -> Unit
) : VideoComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        onRender {
            VideoScreen(this)
        }
    }

    override fun back() {
        onBack()
    }
}