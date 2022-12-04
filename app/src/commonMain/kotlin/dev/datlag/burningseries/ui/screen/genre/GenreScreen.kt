package dev.datlag.burningseries.ui.screen.genre

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.model.SeriesInitialInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(component: GenreComponent) {
    val scaffoldState = rememberScaffoldState()
    val strings = LocalStringRes.current
    val genre by component.genre.collectAsState(null)
    val searchItems by component.searchItems.collectAsState(emptyList())


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
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = strings.previousGenre
                    )
                }
                ExtendedFloatingActionButton(onClick = {
                    component.openSearchBar()
                }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = strings.search
                    )
                    Text(
                        text = strings.search
                    )
                }
                SmallFloatingActionButton(onClick = {
                    component.nextGenre()
                }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = strings.nextGenre
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        scaffoldState = scaffoldState
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                            }.padding(8.dp),
                            text = item.title
                        )
                    }
                }
            } else {
                items(searchItems) { item ->
                    Text(
                        modifier = Modifier.fillMaxWidth().clickable {
                            component.onSeriesClicked(item.href, SeriesInitialInfo(item.title, null))
                        }.padding(8.dp),
                        text = item.title
                    )
                }
            }
        }
    }
}