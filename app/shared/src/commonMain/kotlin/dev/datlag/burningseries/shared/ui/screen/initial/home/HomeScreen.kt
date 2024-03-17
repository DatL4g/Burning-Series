package dev.datlag.burningseries.shared.ui.screen.initial.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.state.HomeState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.WindowSize
import dev.datlag.burningseries.shared.common.lifecycle.calculateWindowWidthSize
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.rememberIsTv
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.HomeOverview
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.SearchFAB
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.SearchOverview
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

