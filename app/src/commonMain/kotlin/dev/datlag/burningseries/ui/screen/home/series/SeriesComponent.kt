package dev.datlag.burningseries.ui.screen.home.series

import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.screen.SeriesItemComponent
import kotlinx.coroutines.flow.Flow

interface SeriesComponent : SeriesItemComponent {

    val status: Flow<Status>
    val series: Flow<List<Home.Series>>

    val latestFavorites: Flow<List<DBSeries>>

}