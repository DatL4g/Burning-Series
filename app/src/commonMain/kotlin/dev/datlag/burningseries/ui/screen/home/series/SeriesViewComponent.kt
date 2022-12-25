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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
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

    private val lastWatchedSeries = db.burningSeriesQueries.selectLatestSeriesAmount(5).asFlow().mapToList(Dispatchers.IO)
    private val lastFavoriteSeries = db.burningSeriesQueries.selectLatestFavoritesAmount(5).asFlow().mapToList(Dispatchers.IO)

    override val latestFavorites = combine(lastWatchedSeries, lastFavoriteSeries) { t1, t2 ->
        return@combine if (t2.isEmpty()) {
            t1
        } else if (t1.isEmpty()) {
            t2
        } else {
            buildSet {
                addAll(t1)
                addAll(t2)
            }.toList()
        }
    }.flowOn(Dispatchers.IO)

    override fun onSeriesClicked(href: String, info: SeriesInitialInfo) {
        onSeries(href, info)
    }

    @Composable
    override fun render() {
        SeriesView(this)
    }
}