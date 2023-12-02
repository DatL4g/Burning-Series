package dev.datlag.burningseries.shared.ui.screen.initial.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.DeviceContent
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.EpisodeItem
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.SeriesItem
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val homeState by component.homeState.collectAsStateWithLifecycle()

    when (val currentState = homeState) {
        is HomeState.Loading -> {
            LoadingState(SharedRes.strings.loading_home)
        }
        is HomeState.Error -> {
            ErrorState(SharedRes.strings.error_loading_home) {
                component.retryLoadingHome()
            }
        }
        is HomeState.Success -> {
            when (calculateWindowSizeClass().widthSizeClass) {
                WindowWidthSizeClass.Expanded -> ExpandedView(currentState.home, component)
                else -> DefaultView(currentState.home, component)
            }
        }
    }
}

@Composable
private fun DefaultView(home: Home, component: HomeComponent) {
    val childState by component.child.subscribeAsState()
    childState.child?.also { (_, instance) ->
        instance.render()
    } ?: MainView(home, component, Modifier.fillMaxWidth())
}

@Composable
private fun ExpandedView(home: Home, component: HomeComponent) {
    val childState by component.child.subscribeAsState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val modifier = if (childState.child?.configuration != null) {
            Modifier.widthIn(max = 700.dp)
        } else {
            Modifier.fillMaxWidth()
        }
        MainView(home, component, modifier)

        childState.child?.also { (_, instance) ->
            Box(
                modifier = Modifier.weight(2F)
            ) {
                instance.render()
            }
        }
    }
}

@Composable
private fun MainView(home: Home, component: HomeComponent, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val state = rememberLazyGridState(
            initialFirstVisibleItemIndex = StateSaver.homeGridIndex,
            initialFirstVisibleItemScrollOffset = StateSaver.homeGridOffset
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(400.dp),
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = state
        ) {
            DeviceContent()
            header {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(SharedRes.strings.newest_episodes),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(home.episodes, key = {
                it.href
            }) { episode ->
                EpisodeItem(episode) {
                    component.itemClicked(HomeConfig.Series(episode))
                }
            }
            header {
                Spacer(modifier = Modifier.size(48.dp))
            }
            header {
                Text(
                    text = stringResource(SharedRes.strings.newest_series),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(home.series, key = {
                it.href
            }) { series ->
                SeriesItem(series) {
                    component.itemClicked(HomeConfig.Series(series))
                }
            }
        }
        VerticalScrollbar(rememberScrollbarAdapter(state))

        DisposableEffect(state) {
            onDispose {
                StateSaver.homeGridIndex = state.firstVisibleItemIndex
                StateSaver.homeGridOffset = state.firstVisibleItemScrollOffset
            }
        }
    }
}