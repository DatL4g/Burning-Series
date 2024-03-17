package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.state.SearchState
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.mergedLocalPadding
import dev.datlag.burningseries.shared.ui.custom.FloatingSearchButton
import dev.datlag.burningseries.shared.ui.screen.initial.home.HomeComponent

@Composable
fun BoxScope.SearchFAB(component: HomeComponent) {
    val searchState by component.searchState.collectAsStateWithLifecycle()

    FloatingSearchButton(
        modifier = Modifier.align(Alignment.BottomEnd).mergedLocalPadding(WindowInsets.ime.asPaddingValues(), 16.dp),
        onTextChange = {
            component.searchQuery(it)
        },
        enabled = searchState !is SearchState.Loading,
        icon = when (searchState) {
            is SearchState.Loading -> Icons.Default.YoutubeSearchedFor
            is SearchState.Success -> Icons.Default.Search
            is SearchState.Error -> Icons.Default.SearchOff
        },
        overrideOnClick = searchState !is SearchState.Success,
        onClick = {
            component.retryLoadingSearch()
        }
    )
}