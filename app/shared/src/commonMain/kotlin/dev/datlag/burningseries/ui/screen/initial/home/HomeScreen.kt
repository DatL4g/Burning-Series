package dev.datlag.burningseries.ui.screen.initial.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.common.header
import dev.datlag.burningseries.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.screen.initial.home.component.EpisodeItem
import dev.datlag.burningseries.ui.screen.initial.home.component.SeriesItem
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val homeState by component.homeState.collectAsStateWithLifecycle()

    when (val currentState = homeState) {
        is HomeState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val painterRes = if (LocalDarkMode.current) {
                    SharedRes.images.movie_dark
                } else {
                    SharedRes.images.movie_light
                }
                Image(
                    modifier = Modifier.fillMaxWidth(0.7F),
                    painter = painterResource(painterRes),
                    contentDescription = stringResource(SharedRes.strings.loading_home)
                )
                Text(
                    text = stringResource(SharedRes.strings.loading_home),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        is HomeState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val painterRes = if (LocalDarkMode.current) {
                    SharedRes.images.sad_dark
                } else {
                    SharedRes.images.sad_light
                }
                Image(
                    modifier = Modifier.fillMaxWidth(0.7F),
                    painter = painterResource(painterRes),
                    contentDescription = stringResource(SharedRes.strings.error_loading_home)
                )
                Text(
                    text = stringResource(SharedRes.strings.error_loading_home),
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = {
                        component.retryLoadingHome()
                    }
                ) {
                    Text(text = stringResource(SharedRes.strings.retry))
                }
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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        header {
            Text(
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
}