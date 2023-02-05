package dev.datlag.burningseries.ui.screen.genre

import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.ui.custom.SearchAppBar
import dev.datlag.burningseries.ui.custom.SearchAppBarState

@Composable
fun GenreScreenAppBar(component: GenreComponent, listFocusRequester: FocusRequester) {
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
                        text = strings.allSeriesHeader,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
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
                onSearchClicked = { listFocusRequester.requestFocus() }
            )
        }
    }
}