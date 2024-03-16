package dev.datlag.burningseries.shared.ui.screen.initial.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.localPadding
import dev.datlag.burningseries.shared.common.mergedLocalPadding
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.rememberIsTv
import dev.datlag.burningseries.shared.ui.custom.FloatingSearchButton
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.DeviceContent
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.EpisodeItem
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.SeriesItem
import dev.datlag.burningseries.shared.ui.theme.MaterialSymbols
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(component: HomeComponent) {
    val homeState by component.homeState.collectAsStateWithLifecycle()
    val dialogState by component.dialog.subscribeAsState()

    when (val currentState = homeState) {
        is HomeState.Loading -> {
            LoadingState(SharedRes.strings.loading_home)
        }
        is HomeState.Error -> {
            val reachable by component.onDeviceReachable.collectAsStateWithLifecycle()

            ErrorState(
                text = SharedRes.strings.error_loading_home,
                customText = {
                    if (!reachable) {
                        Text(
                            modifier = Modifier.fillMaxWidth(0.85F),
                            text = stringResource(SharedRes.strings.enable_custom_dns),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            ) {
                component.retryLoadingHome()
            }
        }
        is HomeState.Success -> {
            when (calculateWindowSizeClass().widthSizeClass) {
                WindowWidthSizeClass.Expanded -> {
                    if (rememberIsTv()) {
                        DefaultView(currentState.home, component)
                    } else {
                        ExpandedView(currentState.home, component)
                    }
                }
                else -> DefaultView(currentState.home, component)
            }
        }
    }

    dialogState.child?.instance?.render()
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
    var rowWidth by remember { mutableIntStateOf(-1) }

    Row(
        modifier = Modifier.onSizeChanged {
            rowWidth = it.width
        },
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val colOne = if (rowWidth > 600) {
            val third = remember(rowWidth) { rowWidth.toFloat() / 3F }
            if (third >= 200F) {
                Modifier.widthIn(max = third.dp)
            } else {
                Modifier.weight(1F)
            }
        } else {
            Modifier.weight(1F)
        }
        MainView(home, component, colOne)

        childState.child?.also { (_, instance) ->
            Box(
                modifier = Modifier.defaultMinSize(minWidth = 400.dp)
            ) {
                instance.render()
            }
        }
    }
}

@Composable
private fun MainView(home: Home, component: HomeComponent, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val state = rememberLazyGridState(
                initialFirstVisibleItemIndex = StateSaver.homeGridIndex,
                initialFirstVisibleItemScrollOffset = StateSaver.homeGridOffset
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(400.dp),
                modifier = Modifier.weight(1F).haze(state = LocalHaze.current),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = LocalPadding(),
                state = state
            ) {
                DeviceContent(component.release, component.onDeviceReachable)
                header {
                    Row(
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            modifier = Modifier.weight(1F),
                            text = stringResource(SharedRes.strings.newest_episodes),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!StateSaver.sekretLibraryLoaded) {
                            IconButton(
                                onClick = {
                                    component.showDialog(DialogConfig.Sekret)
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = MaterialSymbols.rememberDeployedCodeAlert(),
                                    contentDescription = stringResource(SharedRes.strings.sekret_unavailable_title)
                                )
                            }
                        }
                    }
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
        FloatingSearchButton(
            modifier = Modifier.align(Alignment.BottomEnd).mergedLocalPadding(WindowInsets.ime.asPaddingValues(), 16.dp),
            onTextChange = {

            }
        )
    }
}