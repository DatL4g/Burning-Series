package dev.datlag.burningseries.ui.screen.home.episode

import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.network.repository.HomeRepository
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow

interface EpisodesComponent : Component {

    val status: Flow<HomeRepository.Status>
    val episodes: Flow<List<Home.Episode>>

}