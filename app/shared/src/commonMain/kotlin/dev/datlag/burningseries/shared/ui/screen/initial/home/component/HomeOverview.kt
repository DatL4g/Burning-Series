package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.screen.initial.home.DialogConfig
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeComponent
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeConfig
import dev.datlag.burningseries.shared.ui.theme.MaterialSymbols
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun RowScope.HomeOverview(home: Home, component: HomeComponent) {
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