package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.ui.screen.home.episode.GridCellSize

@Composable
fun SeriesView(component: SeriesComponent) {
    val series by component.series.collectAsState(component.series.getValueBlocking(emptyList()))

    LazyVerticalGrid(
        columns = GridCellSize,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        header {
            Text(
                text = "Latest Series"
            )
        }
        items(series) {
            SeriesItem(it, component)
        }
        header {
            Text(
                text = "More Series"
            )
        }
    }
}