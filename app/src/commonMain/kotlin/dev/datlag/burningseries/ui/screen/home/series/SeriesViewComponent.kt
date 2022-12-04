package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.repository.HomeRepository
import org.kodein.di.DI
import org.kodein.di.instance

class SeriesViewComponent(
    componentContext: ComponentContext,
    private val onSeries: (String, SeriesInitialInfo) -> Unit,
    override val di: DI
) : SeriesComponent, ComponentContext by componentContext {

    private val homeRepo: HomeRepository by di.instance()
    override val status = homeRepo.status
    override val series = homeRepo.series

    override fun onSeriesClicked(href: String, info: SeriesInitialInfo) {
        onSeries(href, info)
    }

    @Composable
    override fun render() {
        SeriesView(this)
    }
}