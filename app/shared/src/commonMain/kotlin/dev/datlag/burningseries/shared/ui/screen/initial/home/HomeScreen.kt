package dev.datlag.burningseries.shared.ui.screen.initial.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material3.*
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
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.common.lifecycle.WindowSize
import dev.datlag.burningseries.shared.common.lifecycle.calculateWindowWidthSize
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.mergedLocalPadding
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.rememberIsTv
import dev.datlag.burningseries.shared.ui.custom.FloatingSearchButton
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.*
import dev.datlag.burningseries.shared.ui.theme.MaterialSymbols
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun HomeScreen(component: HomeComponent) {
    val homeState by component.homeState.collectAsStateWithLifecycle()
    val dialogState by component.dialog.subscribeAsState()

    when (val currentState = homeState) {
        is HomeState.Loading -> {
            Box(Modifier.fillMaxHeight()) {
                val searchItems by component.searchItems.collectAsStateWithLifecycle()

                if (searchItems.isEmpty()) {
                    LoadingState(SharedRes.strings.loading_home)
                } else {
                    SearchOverview(searchItems, component, Modifier.fillMaxSize(), 16.dp)
                }

                SearchFAB(component)
            }
        }
        is HomeState.Error -> {
            Box(Modifier.fillMaxHeight()) {
                val reachable by component.onDeviceReachable.collectAsStateWithLifecycle()
                val searchItems by component.searchItems.collectAsStateWithLifecycle()

                if (searchItems.isEmpty()) {
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
                } else {
                    SearchOverview(searchItems, component, Modifier.fillMaxSize(), 16.dp)
                }

                SearchFAB(component)
            }
        }
        is HomeState.Success -> {
            when (calculateWindowWidthSize()) {
                is WindowSize.Expanded -> {
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
    childState.child?.instance?.render() ?: MainView(home, component, Modifier.fillMaxWidth())
}

@Composable
private fun ExpandedView(home: Home, component: HomeComponent) {
    val childState by component.child.subscribeAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MainView(home, component, Modifier.weight(1.5F))

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
    Box(modifier = modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val searchItems by component.searchItems.collectAsStateWithLifecycle()

            if (searchItems.isEmpty()) {
                HomeOverview(home, component)
            } else {
                SearchOverview(searchItems, component, Modifier.weight(1F))
            }
        }

        SearchFAB(component)
    }
}

