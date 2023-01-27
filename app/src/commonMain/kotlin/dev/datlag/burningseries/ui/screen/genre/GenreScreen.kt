package dev.datlag.burningseries.ui.screen.genre

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.FabPosition
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.OnWarning
import dev.datlag.burningseries.common.Warning
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.screen.home.LocalFabGroupRequester
import dev.datlag.burningseries.ui.Shape
import dev.datlag.burningseries.ui.custom.snackbarHandlerForStatus
import androidx.compose.material.SnackbarDefaults
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.other.StateSaver

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun GenreScreen(component: GenreComponent) {
    val strings = LocalStringRes.current
    val genre by component.genre.collectAsState(null)
    val searchItems by component.searchItems.collectAsState(emptyList())
    val (previousFab, nextFab) = FocusRequester.createRefs()

    val snackbarHostState = remember { SnackbarHostState() }
    val scaffoldState = rememberScaffoldState(
        snackbarHostState = snackbarHostState
    )

    val colorScheme = MaterialTheme.colorScheme
    val defaultColors = SnackbarDefaults.backgroundColor to androidx.compose.material.MaterialTheme.colors.surface
    var snackbarColors by remember { mutableStateOf(defaultColors) }

    snackbarHandlerForStatus(
        state = snackbarHostState,
        status = component.status,
        mapper = {
            when (it) {
                is Status.LOADING -> strings.loadingAll
                is Status.ERROR.TOO_MANY_REQUESTS -> strings.tooManyRequests
                is Status.ERROR -> strings.errorTryAgain
                else -> null
            }
        }
    ) { status ->
        snackbarColors = when (status) {
            is Status.LOADING -> Color.Warning to Color.OnWarning
            is Status.ERROR -> colorScheme.error to colorScheme.onError
            else -> defaultColors
        }
    }

    Scaffold(
        topBar = {
            GenreScreenAppBar(component)
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallFloatingActionButton(onClick = {
                    component.previousGenre()
                }, modifier = Modifier.focusRequester(previousFab)) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = strings.previousGenre
                    )
                }
                ExtendedFloatingActionButton(onClick = {
                    component.openSearchBar()
                }, modifier = Modifier) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = strings.search
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = strings.search
                    )
                }
                SmallFloatingActionButton(onClick = {
                    component.nextGenre()
                }, modifier = Modifier.focusRequester(nextFab)) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = strings.nextGenre
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    snackbarData = data,
                    backgroundColor = snackbarColors.first,
                    contentColor = snackbarColors.second,
                    shape = Shape.FullRoundedShape,
                    elevation = 0.dp,
                    actionOnNewLine = false
                )
            }
        }
    ) {
        val state = rememberLazyListState(
            StateSaver.genreViewPos,
            StateSaver.genreViewOffset
        )

        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            if (searchItems.isEmpty()) {
                genre?.let { safeGenre ->
                    item(safeGenre) {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            text = safeGenre.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    items(safeGenre.items) { item ->
                        Text(
                            modifier = Modifier.fillMaxWidth().clickable {
                                component.onSeriesClicked(item.href, SeriesInitialInfo(item.title, null))
                            }.padding(8.dp).focusProperties {
                                left = previousFab
                                right = nextFab
                            },
                            text = item.title,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true
                        )
                    }
                }
            } else {
                items(searchItems) { item ->
                    Text(
                        modifier = Modifier.fillMaxWidth().clickable {
                            component.onSeriesClicked(item.href, SeriesInitialInfo(item.title, null))
                        }.padding(8.dp).focusProperties {
                            left = previousFab
                            right = nextFab
                        },
                        text = item.title,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
            }
        }
    }
}