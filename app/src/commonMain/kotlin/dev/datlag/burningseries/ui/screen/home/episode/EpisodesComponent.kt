package dev.datlag.burningseries.ui.screen.home.episode

import dev.datlag.burningseries.database.SelectLatestEpisodesAmount
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.navigation.Component
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EpisodesComponent : Component {

    val status: Flow<Status>
    val episodes: Flow<List<Home.Episode>>

    val imageDir: File
    val lastWatched: Flow<List<SelectLatestEpisodesAmount>>

    fun onEpisodeClicked(href: String, initialInfo: SeriesInitialInfo, continueWatching: Boolean)
}