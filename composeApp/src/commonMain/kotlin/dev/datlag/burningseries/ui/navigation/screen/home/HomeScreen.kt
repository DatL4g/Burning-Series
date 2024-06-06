package dev.datlag.burningseries.ui.navigation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.YoutubeSearchedFor
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.datlag.burningseries.LocalHaze
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.network.state.HomeState
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.burningseries.ui.custom.AndroidFixWindowSize
import dev.datlag.burningseries.ui.custom.FloatingSearchButton
import dev.datlag.burningseries.ui.custom.scrollbar.LazyColumnScrollbar
import dev.datlag.burningseries.ui.custom.scrollbar.ScrollbarSettings
import dev.datlag.burningseries.ui.navigation.screen.component.CollapsingToolbar
import dev.datlag.burningseries.ui.navigation.screen.home.component.CompactScreen
import dev.datlag.burningseries.ui.navigation.screen.home.component.WideScreen
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    AndroidFixWindowSize {
        val appBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            state = appBarState
        )
        val searchState by component.search.collectAsStateWithLifecycle()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CollapsingToolbar(
                    state = appBarState,
                    scrollBehavior = scrollBehavior,
                    onSettingsClick = { }
                )
            },
            floatingActionButton = {
                FloatingSearchButton(
                    modifier = Modifier.padding(WindowInsets.ime.asPaddingValues()),
                    onTextChange = {
                        component.search(it)
                    },
                    enabled = !searchState.isLoading,
                    icon = when (searchState) {
                        is SearchState.Loading -> Icons.Rounded.YoutubeSearchedFor
                        is SearchState.Success -> Icons.Rounded.Search
                        is SearchState.Failure -> Icons.Rounded.SearchOff
                    },
                    overrideOnClick = searchState !is SearchState.Success,
                    onClick = {
                        // overwritten click
                        component.retryLoadingSearch()
                    }
                )
            }
        ) { padding ->
            val state by component.home.collectAsStateWithLifecycle()

            if (searchState.hasQueryItems) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().haze(state = LocalHaze.current),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    contentPadding = padding
                ) {
                    items(
                        items = (searchState as? SearchState.Success)?.queriedItems
                            .orEmpty()
                            .toImmutableList(),
                        key = { it.href }
                    ) {
                        ElevatedCard(
                            modifier = Modifier.fillParentMaxWidth(),
                            onClick = { component.details(it) },
                            shape = MaterialTheme.shapes.extraSmall,
                            elevation = CardDefaults.elevatedCardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxHeight().weight(1F),
                                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = it.mainTitle,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = if (it.hasSubtitle) 1 else 2
                                    )
                                    it.subTitle?.let { sub ->
                                        Text(
                                            text = sub,
                                            maxLines = 1
                                        )
                                    }
                                }
                                it.genre?.let { genre ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = {
                                            Text(text = genre)
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (it.isAnime) {
                                                MaterialTheme.colorScheme.primary
                                            } else Color.Unspecified,
                                            labelColor = if (it.isAnime) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else Color.Unspecified
                                        ),
                                        border = if (it.isAnime) null else SuggestionChipDefaults.suggestionChipBorder(true)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                when (calculateWindowSizeClass().widthSizeClass) {
                    WindowWidthSizeClass.Compact -> CompactScreen(state, padding, component)
                    else -> WideScreen(state, padding, component)
                }
            }
        }
    }
}
