package dev.datlag.burningseries.ui.screen.initial.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.OnBottomReached
import dev.datlag.burningseries.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.ui.custom.state.ErrorState
import dev.datlag.burningseries.ui.custom.state.LoadingState
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalFoundationApi::class)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.fillMaxWidth(),
                                text = genre.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        items(genre.items) {
                            Text(
                                modifier = Modifier.fillMaxWidth().onClick {

                                }.padding(8.dp),
                                text = it.title,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (canLoadMore) {
                        item(key = canLoadMore) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
            AnimatedVisibility(visible = queryComp.isNotBlank()) {
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
                modifier = Modifier.fillMaxWidth().onClick {

                }.padding(8.dp),
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