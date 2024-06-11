package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.fullRow
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.ui.navigation.screen.component.CollapsingToolbar
import dev.datlag.burningseries.ui.navigation.screen.component.HomeCard
import dev.datlag.burningseries.ui.navigation.screen.home.HomeComponent
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import dev.datlag.burningseries.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.ui.custom.localScrollbarStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WideScreen(
    state: HomeState,
    padding: PaddingValues,
    component: HomeComponent
) {
    val listState = rememberLazyGridState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            state = listState,
            modifier = Modifier.fillMaxSize().haze(state = LocalHaze.current),
            columns = GridCells.FixedSize(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = padding.merge(16.dp)
        ) {
            fullRow {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Episodes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            when {
                state.isLoading -> fullRow {
                    Loading()
                }
                state.isEpisodeError -> fullRow {
                    Text(text = "Error loading episodes")
                }
                state is HomeState.Success -> {
                    items(
                        items = (state as? HomeState.Success)
                            ?.home
                            ?.episodes
                            .orEmpty()
                            .toImmutableList(),
                        key = { it.href }
                    ) {
                        HomeCard(
                            episode = it,
                            modifier = Modifier
                                .width(200.dp)
                                .height(280.dp),
                            onClick = component::details
                        )
                    }
                }
            }
            fullRow {
                Text(
                    text = "Series",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            when {
                state.isLoading -> fullRow {
                    Loading()
                }
                state.isSeriesError -> fullRow {
                    Text(text = "Error loading series")
                }
                state is HomeState.Success -> {
                    items(
                        items = (state as? HomeState.Success)
                            ?.home
                            ?.series
                            .orEmpty()
                            .toImmutableList(),
                        key = { it.href }
                    ) {
                        HomeCard(
                            series = it,
                            modifier = Modifier
                                .width(200.dp)
                                .height(280.dp),
                            onClick = component::details
                        )
                    }
                }
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.padding(horizontal = 4.dp).fillMaxHeight().align(Alignment.TopEnd),
            style = localScrollbarStyle().copy(
                unhoverColor = MaterialTheme.colorScheme.secondary,
                hoverColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier.fillMaxWidth().height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(fraction = 0.2F).clip(CircleShape)
        )
    }
}