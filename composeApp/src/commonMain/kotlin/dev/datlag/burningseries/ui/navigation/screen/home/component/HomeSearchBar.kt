package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.merge
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.search
import dev.datlag.burningseries.network.state.SearchState
import dev.datlag.burningseries.ui.navigation.screen.home.HomeComponent
import dev.datlag.tooling.compose.onClick
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSearchBar(component: HomeComponent, showFavorites: Boolean) {
    var query by remember { mutableStateOf("") }
    val searchState by component.search.collectAsStateWithLifecycle()
    val windowInsets = SearchBarDefaults.windowInsets.asPaddingValues().merge(16.dp)
    var isActive by remember(searchState) { mutableStateOf(searchState.hasQueryItems) }

    DockedSearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(windowInsets),
        query = query,
        onQueryChange = {
            query = it
            component.search(query)
        },
        active = isActive,
        onActiveChange = {
            if (it) {
                isActive = searchState.hasQueryItems
                if (searchState.isError) {
                    component.retryLoadingSearch()
                }
            } else {
                isActive = false
            }
        },
        onSearch = {
            query = it
            component.search(it)
        },
        leadingIcon = {
            IconButton(
                onClick = {
                    if (isActive) {
                        isActive = false
                    } else {
                        // open settings
                    }
                }
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.Settings,
                    contentDescription = null
                )
            }
        },
        placeholder = {
            Text(text = stringResource(Res.string.search))
        },
        trailingIcon = {
            if (isActive) {
                IconButton(
                    onClick = {
                        query = ""
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = null
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        component.toggleFavorites()
                    }
                ) {
                    Icon(
                        imageVector = if (showFavorites) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        contentDescription = null
                    )
                }
            }
        },
        content = {
            val language by component.language.collectAsStateWithLifecycle(null)

            when (val current = searchState) {
                is SearchState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(fraction = 0.2F).clip(CircleShape)
                        )
                    }
                }
                is SearchState.Failure -> {
                    Text(text = "Error")
                }
                is SearchState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(current.queriedItems.toImmutableList(), key = { it.href }) {
                            SearchResult(
                                item = it,
                                modifier = Modifier.fillParentMaxWidth(),
                                onClick = { data ->
                                    component.details(data, language)
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
