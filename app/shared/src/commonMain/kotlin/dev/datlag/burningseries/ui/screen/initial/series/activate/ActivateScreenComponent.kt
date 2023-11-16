package dev.datlag.burningseries.ui.screen.initial.series.activate

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import dev.datlag.burningseries.model.Series
import org.kodein.di.DI
import dev.datlag.burningseries.SharedRes

class ActivateScreenComponent(
    componentContext: ComponentContext,
    override val di: DI,
    override val episode: Series.Episode,
    private val onGoBack: () -> Unit
) : ActivateComponent, ComponentContext by componentContext {

    override val scrapingJs: String = SharedRes.assets.scrape_hoster.readText()

    private val backCallback = BackCallback {
        onGoBack()
    }

    init {
        backHandler.register(backCallback)
    }

    @Composable
    override fun render() {
        ActivateScreen(this)
    }

    override fun back() {
        onGoBack()
    }
}