package dev.datlag.burningseries.ui.screen.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.collectAsStateSafe
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.common.isTv
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.custom.SearchAppBarState
import dev.datlag.burningseries.ui.screen.home.gridCellSize
import dev.datlag.burningseries.ui.screen.home.series.SeriesItem

@Composable
fun FavoriteScreen(component: FavoriteComponent) {
    val listRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            FavoriteScreenAppBar(component, listRequester)
        },
        floatingActionButton = {
            val searchState by component.searchAppBarState.subscribeAsState()

            if (searchState is SearchAppBarState.CLOSED) {
                FloatingActionButton(
                    onClick = {
                        component.openSearchBar()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = LocalStringRes.current.search
                    )
                }
            }
        }
    ) {
        val favorites by component.favorites.collectAsStateSafe { component.favorites.getValueBlocking(emptyList()) }
        val searchFavorites by component.searchItems.collectAsStateSafe { emptyList() }

        if (isTv()) {
            val state = rememberLazyListState(
                StateSaver.favoriteViewPos,
                StateSaver.favoriteViewOffset
            )

            LazyRow(
                state = state,
                modifier = Modifier.fillMaxSize().focusRequester(listRequester),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(searchFavorites.ifEmpty { favorites }, key = { it.hrefTitle }) {
                    SeriesItem(it, component)
                }
            }
        } else {
            val state = rememberLazyGridState(
                StateSaver.favoriteViewPos,
                StateSaver.favoriteViewOffset
            )

            LazyVerticalGrid(
                state = state,
                modifier = Modifier.focusRequester(listRequester),
                columns = gridCellSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchFavorites.ifEmpty { favorites }, key = { it.hrefTitle }) {
                    SeriesItem(it, component)
                }
            }
        }
    }
}