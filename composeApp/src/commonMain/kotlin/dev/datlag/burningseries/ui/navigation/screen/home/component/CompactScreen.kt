package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.navigation.screen.component.HomeCard
import dev.datlag.burningseries.ui.navigation.screen.home.HomeComponent
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import dev.datlag.burningseries.ui.custom.HorizontalScrollbar
import dev.datlag.burningseries.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.ui.custom.localScrollbarStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompactScreen(
    state: HomeState,
    padding: PaddingValues,
    component: HomeComponent
) {
    val language by component.language.collectAsStateWithLifecycle(null)

    LazyColumn(
        modifier = Modifier.fillMaxSize().haze(state = LocalHaze.current),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = padding
    ) {
        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Episodes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            when {
                state.isLoading -> {
                    Loading()
                }
                state.isEpisodeError -> {
                    Text(text = "Error loading episodes")
                }
                state is HomeState.Success -> {
                    val rowState = rememberLazyListState()

                    Column(
                        modifier = Modifier.fillParentMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LazyRow(
                            state = rowState,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(
                                items = (state as? HomeState.Success)
                                    ?.home
                                    ?.episodes
                                    .orEmpty()
                                    .sortedBy { Language.fromString(it.language)?.compareToNullable(language) }
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

                        HorizontalScrollbar(
                            adapter = rememberScrollbarAdapter(rowState),
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                            style = localScrollbarStyle().copy(
                                unhoverColor = MaterialTheme.colorScheme.secondary,
                                hoverColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Series",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            when {
                state.isLoading -> {
                    Loading()
                }
                state.isSeriesError -> {
                    Text(text = "Error loading series")
                }
                state is HomeState.Success -> {
                    val rowState = rememberLazyListState()

                    Column(
                        modifier = Modifier.fillParentMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LazyRow(
                            state = rowState,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
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
                                    onClick = { data ->
                                        component.details(data, language)
                                    }
                                )
                            }
                        }

                        HorizontalScrollbar(
                            adapter = rememberScrollbarAdapter(rowState),
                            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                            style = localScrollbarStyle().copy(
                                unhoverColor = MaterialTheme.colorScheme.secondary,
                                hoverColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
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