package dev.datlag.burningseries.shared.ui.screen.initial.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.OnBottomReached
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.onClick
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SearchScreen(component: SearchComponent) {
    val state by component.searchState.collectAsStateWithLifecycle()

    when (state) {
        is SearchState.Loading -> {
            LoadingState(SharedRes.strings.loading_search)
        }
        is SearchState.Error -> {
            ErrorState(SharedRes.strings.error_loading_search) {
                component.retryLoadingSearch()
            }
        }
        is SearchState.Success -> {
            when (calculateWindowSizeClass().widthSizeClass) {
                WindowWidthSizeClass.Expanded -> ExpandedView(component)
                else -> DefaultView(component)
            }
        }
    }
}

@Composable
private fun DefaultView(component: SearchComponent) {
    val childState by component.child.subscribeAsState()
    childState.child?.also { (_, instance) ->
        instance.render()
    } ?: MainView(component, Modifier.fillMaxWidth())
}

@Composable
private fun ExpandedView(component: SearchComponent) {
    val childState by component.child.subscribeAsState()

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val modifier = if (childState.child?.configuration != null) {
            Modifier.widthIn(max = 700.dp)
        } else {
            Modifier.fillMaxWidth()
        }
        MainView(component, modifier)

        childState.child?.also { (_, instance) ->
            Box(
                modifier = Modifier.weight(2F)
            ) {
                instance.render()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainView(component: SearchComponent, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val genres by component.genres.collectAsStateWithLifecycle()
        val canLoadMore by component.canLoadMoreGenres.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier.weight(1F),
            state = listState
        ) {
            stickyHeader {
                SearchBar(component)
            }
            genres.forEach { genre ->
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().animateItemPlacement(),
                        text = genre.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                items(genre.items) {
                    Text(
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraSmall).onClick {
                            component.itemClicked(SearchConfig.Series(it))
                        }.padding(12.dp).animateItemPlacement(),
                        text = it.title,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (canLoadMore) {
                item(key = canLoadMore) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).animateItemPlacement(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        VerticalScrollbar(rememberScrollbarAdapter(listState))

        listState.OnBottomReached(canLoadMore) {
            component.loadMoreGenres()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(component: SearchComponent) {
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
        modifier = Modifier.fillMaxWidth().padding(8.dp).animateContentSize(),
        active = queryComp.isNotBlank() && items.isNotEmpty(),
        onActiveChange = {},
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
                    component.itemClicked(SearchConfig.Series(item))
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