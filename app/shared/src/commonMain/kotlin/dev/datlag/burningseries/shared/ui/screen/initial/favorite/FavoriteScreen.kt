package dev.datlag.burningseries.shared.ui.screen.initial.favorite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.header
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.onClick
import dev.datlag.burningseries.shared.rememberIsTv
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.screen.initial.home.component.SeriesItem
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun FavoriteScreen(component: FavoriteComponent) {
    when (calculateWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Expanded -> {
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

        MainView(component, colOne)

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
private fun MainView(component: FavoriteComponent, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val favorites by component.favorites.collectAsStateWithLifecycle()
        val listState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(400.dp),
            modifier = Modifier.weight(1F),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = LocalPadding()
        ) {
            header {
                SearchBar(component)
            }
            items(favorites, key = { it.hrefPrimary }) { fav ->
                SeriesItem(fav) {
                    component.itemClicked(FavoriteConfig.Series(fav))
                }
            }
        }
        VerticalScrollbar(rememberScrollbarAdapter(listState))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(component: FavoriteComponent) {
    val items by component.searchItems.collectAsStateWithLifecycle()
    var queryComp by remember { mutableStateOf(String()) }

    DockedSearchBar(
        query = queryComp,
        onQueryChange = {
            queryComp = it
        },
        onSearch = {
            queryComp = it
        },
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        active = queryComp.isNotBlank() && items.isNotEmpty(),
        onActiveChange = {},
        placeholder = {
            Text(text = stringResource(SharedRes.strings.search))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(SharedRes.strings.search)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = queryComp.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {
                        queryComp = String()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(SharedRes.strings.clear)
                    )
                }
            }
        }
    ) {
        items.forEach { item ->
            Text(
                modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraSmall).onClick {
                    component.itemClicked(FavoriteConfig.Series(item))
                }.padding(12.dp),
                text = item.title,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    LaunchedEffect(queryComp) {
        component.searchQuery(queryComp)
    }
}