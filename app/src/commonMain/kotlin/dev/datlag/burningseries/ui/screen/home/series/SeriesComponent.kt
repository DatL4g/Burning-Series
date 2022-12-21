package dev.datlag.burningseries.ui.screen.home.series

import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.network.repository.HomeRepository
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface SeriesComponent : Component {

    val status: Flow<Status>
    val series: Flow<List<Home.Series>>

    fun onSeriesClicked(href: String, info: SeriesInitialInfo)

}