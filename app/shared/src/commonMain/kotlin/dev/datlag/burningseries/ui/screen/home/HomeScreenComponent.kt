package dev.datlag.burningseries.ui.screen.home

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.common.ioScope
import dev.datlag.burningseries.common.launchIO
import dev.datlag.burningseries.network.scraper.BurningSeries
import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.instance

class HomeScreenComponent(
    componentContext: ComponentContext,
    override val di: DI
) : HomeComponent, ComponentContext by componentContext {

    private val client: HttpClient by di.instance()

    @Composable
    override fun render() {
        HomeScreen(this)
    }

    init {
        ioScope().launchIO {
            BurningSeries.testSeries(client)
        }
    }
}