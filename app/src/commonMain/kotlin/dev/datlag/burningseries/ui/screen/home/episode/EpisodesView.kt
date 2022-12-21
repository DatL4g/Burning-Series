package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.ui.screen.home.gridCellSize

@Composable
fun EpisodesView(component: EpisodesComponent) {
    val episodes by component.episodes.collectAsState(component.episodes.getValueBlocking(emptyList()))

    LazyVerticalGrid(
        columns = gridCellSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        header {
            Text(
                text = "Latest Episodes"
            )
        }
        items(episodes) {
            EpisodeItem(it)
        }
    }
}
