package dev.datlag.burningseries.ui.screen.home.series

import dev.datlag.burningseries.network.model.Home
import dev.datlag.burningseries.network.repository.HomeRepository
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface SeriesComponent : Component {

    val status: Flow<HomeRepository.Status>
    val series: Flow<List<Home.Series>>

}