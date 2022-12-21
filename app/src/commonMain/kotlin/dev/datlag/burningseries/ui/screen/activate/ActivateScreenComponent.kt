package dev.datlag.burningseries.ui.screen.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.Series
import org.kodein.di.DI

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val series: Series,
    override val episode: Series.Episode,
    override val onGoBack: () -> Unit,
    override val di: DI
) : ActivateComponent, ComponentContext by componentContext {

    @Composable
    override fun render() {
        ActivateScreen(this)
    }
}