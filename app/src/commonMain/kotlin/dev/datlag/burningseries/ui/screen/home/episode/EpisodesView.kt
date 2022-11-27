package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.network.model.Cover
import dev.datlag.burningseries.network.model.Home

@Composable
fun EpisodesView(component: EpisodesComponent) {
    val episodes by component.episodes.collectAsState(emptyList())

    LazyVerticalGrid(
        columns = GridCellSize,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(episodes) {
            EpisodeItem(it)
        }
    }
}

expect val GridCellSize: GridCells