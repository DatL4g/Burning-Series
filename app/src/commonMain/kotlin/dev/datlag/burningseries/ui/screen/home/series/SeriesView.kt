package dev.datlag.burningseries.ui.screen.home.series

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.collectAsStateSafe
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.common.isTv
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.screen.home.gridCellSize

@Composable
fun SeriesView(component: SeriesComponent) {
    val series by component.series.collectAsStateSafe { component.series.getValueBlocking(emptyList()) }
    val favorites by component.latestFavorites.collectAsStateSafe { component.latestFavorites.getValueBlocking(emptyList()) }

    if (isTv()) {
        val state = rememberLazyListState(
            StateSaver.homeSeriesViewPos,
            StateSaver.homeSeriesViewOffset
        )

        LazyRow(
            state = state,
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(favorites, key = { it.href }) {
                SeriesItem(it, component)
            }
            item {
                if (favorites.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
            items(series, key = { it.href }) {
                SeriesItem(it, component)
            }
        }

        DisposableEffect(state) {
            onDispose {
                StateSaver.homeSeriesViewPos = state.firstVisibleItemIndex
                StateSaver.homeSeriesViewOffset = state.firstVisibleItemScrollOffset
            }
        }
    } else {
        val state = rememberLazyGridState(
            StateSaver.homeSeriesViewPos,
            StateSaver.homeSeriesViewOffset
        )

        LazyVerticalGrid(
            state = state,
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
                items(favorites, key = { it.href }) {
                    SeriesItem(it, component)
                }
            }
            header {
                Text(
                    text = LocalStringRes.current.latestSeries,
                    fontWeight = FontWeight.Bold
                )
            }
            items(series, key = { it.href }) {
                SeriesItem(it, component)
            }
        }

        DisposableEffect(state) {
            onDispose {
                StateSaver.homeSeriesViewPos = state.firstVisibleItemIndex
                StateSaver.homeSeriesViewOffset = state.firstVisibleItemScrollOffset
            }
        }
    }
}