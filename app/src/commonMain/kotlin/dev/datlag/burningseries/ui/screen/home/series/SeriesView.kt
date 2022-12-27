package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.ui.screen.home.gridCellSize

@Composable
fun SeriesView(component: SeriesComponent) {
    val series by component.series.collectAsState(component.series.getValueBlocking(emptyList()))
    val favorites by component.latestFavorites.collectAsState(component.latestFavorites.getValueBlocking(emptyList()))

    LazyVerticalGrid(
        columns = gridCellSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (favorites.isNotEmpty()) {
            header {
                Text(
                    text = LocalStringRes.current.favorites,
                    fontWeight = FontWeight.Bold
                )
            }
            items(favorites) {
                SeriesItem(it, component)
            }
        }
        header {
            Text(
                text = LocalStringRes.current.latestSeries,
                fontWeight = FontWeight.Bold
            )
        }
        items(series) {
            SeriesItem(it, component)
        }
    }
}