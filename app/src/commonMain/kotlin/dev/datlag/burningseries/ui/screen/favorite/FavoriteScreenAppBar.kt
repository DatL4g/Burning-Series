package dev.datlag.burningseries.ui.screen.favorite

import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.ui.custom.SearchAppBar
import dev.datlag.burningseries.ui.custom.SearchAppBarState

@Composable
fun FavoriteScreenAppBar(component: FavoriteComponent, listRequester: FocusRequester) {
    val searchAppBarState by component.searchAppBarState.subscribeAsState()
    val strings = LocalStringRes.current

    when (searchAppBarState) {
        is SearchAppBarState.CLOSED -> {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        component.onGoBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = strings.back,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                },
                title = {
                    Text(
                        text = strings.favorites,
                        color = MaterialTheme.colorScheme.onTertiary,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                },
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                elevation = 0.dp
            )
        }
        is SearchAppBarState.OPENED -> {
            val searchText by component.searchText.subscribeAsState()

            SearchAppBar(
                text = searchText,
                placeholder = strings.searchForSeries,
                onTextChange = { component.updateSearchText(it) },
                onCloseClicked = { component.closeSearchBar() },
                onSearchClicked = { listRequester.requestFocus() }
            )
        }
    }
}