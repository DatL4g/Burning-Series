package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dev.datlag.burningseries.common.coroutineScope
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.File

class SeriesViewComponent(
    componentContext: ComponentContext,
    private val onSeries: (String, SeriesInitialInfo) -> Unit,
    override val di: DI
) : SeriesComponent, ComponentContext by componentContext {

    private val homeRepo: HomeRepository by di.instance()
    override val status = homeRepo.status
    override val series = homeRepo.series

    private val db: BurningSeriesDB by di.instance()
    override val imageDir: File by di.instance("ImageDir")

    override val latestFavorites = db.burningSeriesQueries.selectLatestAmount(5).asFlow().mapToList(Dispatchers.IO)

    override fun onSeriesClicked(href: String, info: SeriesInitialInfo) {
        onSeries(href, info)
    }

    @Composable
    override fun render() {
        SeriesView(this)
    }
}