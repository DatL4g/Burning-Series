package dev.datlag.burningseries.shared.ui.screen.initial.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.common.lifecycle.WindowSize
import dev.datlag.burningseries.shared.common.lifecycle.calculateWindowWidthSize
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.mergedLocalPadding
import dev.datlag.burningseries.shared.rememberIsTv
import dev.datlag.burningseries.shared.ui.custom.FloatingSearchButton
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.screen.initial.favorite.component.SeriesCard
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun FavoriteScreen(component: FavoriteComponent) {
    when (calculateWindowWidthSize()) {
        is WindowSize.Expanded -> {
            if (rememberIsTv()) {
                DefaultView(component)
            } else {
                ExpandedView(component)
            }
        }
        else -> DefaultView(component)
    }
}

@Composable
private fun DefaultView(component: FavoriteComponent) {
    val childState by component.child.subscribeAsState()
    childState.child?.also { (_, instance) ->
        instance.render()
    } ?: MainView(component, Modifier.fillMaxWidth())
}

@Composable
private fun ExpandedView(component: FavoriteComponent) {
    val childState by component.child.subscribeAsState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MainView(component, Modifier.weight(1F))

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
private fun MainView(component: FavoriteComponent, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val favorites by component.searchItems.collectAsStateWithLifecycle()
            val listState = rememberLazyGridState()

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.weight(1F).haze(state = LocalHaze.current),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = LocalPadding()
            ) {
                header {
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = stringResource(SharedRes.strings.favorites),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(favorites, key = { it.hrefPrimary }) { fav ->
                    SeriesCard(
                        series = fav,
                        modifier = Modifier.width(150.dp).height(230.dp),
                        onClick = {
                            component.itemClicked(FavoriteConfig.Series(fav))
                        }
                    )
                }
            }
            VerticalScrollbar(rememberScrollbarAdapter(listState))
        }
        FloatingSearchButton(
            modifier = Modifier.align(Alignment.BottomEnd).mergedLocalPadding(WindowInsets.ime.asPaddingValues(), 16.dp),
            onTextChange = {
                component.searchQuery(it)
            }
        )
    }
}
