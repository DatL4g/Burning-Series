package dev.datlag.burningseries.ui.screen.initial.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.datlag.burningseries.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.custom.state.ErrorState
import dev.datlag.burningseries.ui.custom.state.LoadingState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(component: SearchComponent) {
    val state by component.searchState.collectAsStateWithLifecycle()

    when (val current = state) {
        is SearchState.Loading -> {
            LoadingState(SharedRes.strings.loading_search)
        }
        is SearchState.Error -> {
            ErrorState(SharedRes.strings.error_loading_search) {
                component.retryLoadingSearch()
            }
        }
        is SearchState.Success -> {
            val selectedGenre = current.genres.first()

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                stickyHeader {
                    Text(
                        text = selectedGenre.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(selectedGenre.items) {
                    Text(text = it.title)
                }
            }
        }
    }
}