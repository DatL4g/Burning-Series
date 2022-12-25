package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import dev.datlag.burningseries.database.BurningSeriesDB
import dev.datlag.burningseries.database.DBEpisode
import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.database.SelectLatestEpisodesAmount
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import java.io.File

class EpisodesViewComponent(
    componentContext: ComponentContext,
    private val onEpisode: (String, SeriesInitialInfo, Boolean) -> Unit,
    override val di: DI
) : EpisodesComponent, ComponentContext by componentContext {

    private val homeRepo: HomeRepository by di.instance()
    override val status = homeRepo.status
    override val episodes = homeRepo.episodes

    private val db: BurningSeriesDB by di.instance()
    override val imageDir: File by di.instance("ImageDir")
    override val lastWatched: Flow<List<SelectLatestEpisodesAmount>> = db.burningSeriesQueries.selectLatestEpisodesAmount(5).asFlow().mapToList(Dispatchers.IO)

    override fun onEpisodeClicked(
        href: String,
        initialInfo: SeriesInitialInfo,
        continueWatching: Boolean
    ) {
        onEpisode(href, initialInfo, continueWatching)
    }

    @Composable
    override fun render() {
        EpisodesView(this)
    }
}