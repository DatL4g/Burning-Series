package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.ui.screen.home.gridCellSize

@Composable
fun EpisodesView(component: EpisodesComponent) {
    val episodes by component.episodes.collectAsState(component.episodes.getValueBlocking(emptyList()))
    val lastWatched by component.lastWatched.collectAsState(component.lastWatched.getValueBlocking(emptyList()))

    LazyVerticalGrid(
        columns = gridCellSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (lastWatched.isNotEmpty()) {
            header {
                Text(
                    text = LocalStringRes.current.lastWatched,
                    fontWeight = FontWeight.Bold
                )
            }
            items(lastWatched) {
                EpisodeItem(it, component)
            }
        }
        header {
            Text(
                text = LocalStringRes.current.latestEpisodes,
                fontWeight = FontWeight.Bold
            )
        }
        items(episodes) {
            EpisodeItem(it, component)
        }
    }
}
