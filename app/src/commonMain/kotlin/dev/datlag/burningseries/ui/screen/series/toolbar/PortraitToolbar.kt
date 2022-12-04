package dev.datlag.burningseries.ui.screen.series.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.other.DefaultValue
import dev.datlag.burningseries.ui.custom.ArcShape
import dev.datlag.burningseries.ui.custom.CoverImage
import dev.datlag.burningseries.ui.custom.collapsingtoolbar.DefaultCollapsingToolbar
import dev.datlag.burningseries.ui.custom.collapsingtoolbar.rememberCollapsingToolbarScaffoldState
import dev.datlag.burningseries.ui.screen.series.SeriesComponent
import kotlin.math.abs

@Composable
fun PortraitToolbar(
    component: SeriesComponent,
    content: LazyListScope.() -> Unit
) {
    val _title by component.title.collectAsState(component.initialInfo.title)
    val title = _title ?: component.initialInfo.title
    val _cover by component.cover.collectAsState(component.initialInfo.cover)
    val cover = _cover ?: component.initialInfo.cover

    val selectedLanguage by component.selectedLanguage.collectAsState(null)
    val languages by component.languages.collectAsState(emptyList())
    val season = when (val value = component.season.collectAsState(DefaultValue.INITIAL_LOADING("Loading Season")).value) {
        is DefaultValue.INITIAL_LOADING -> value.data
        is DefaultValue.VALUE -> value.data
    }

    val state = rememberCollapsingToolbarScaffoldState()
    val reversedProgress by remember {
        derivedStateOf { (abs(1F - state.toolbarState.progress)) }
    }

    DefaultCollapsingToolbar(
        state = state,
        expandedBody = {
            var titleHeight by remember { mutableStateOf(0) }

            if (cover != null) {
                CoverImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 320.dp)
                        .parallax(ratio = 0.5F)
                        .padding(bottom = with(LocalDensity.current) {
                            titleHeight.toDp() + 16.dp
                        }),
                    cover = cover,
                    description = title,
                    scale = ContentScale.FillWidth,
                    shape = ArcShape(with(LocalDensity.current) {
                        20.dp.toPx()
                    })
                )
            }

            Text(
                text = title,
                modifier = Modifier
                    .road(Alignment.TopStart, Alignment.BottomStart)
                    .padding(16.dp).onSizeChanged {
                        titleHeight = it.height
                    },
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = run {
                    val alpha = state.toolbarState.progress
                    if (alpha < 0.7F) {
                        if (alpha < 0.3F) {
                            0F
                        } else {
                            alpha
                        }
                    } else {
                        1F
                    }
                }),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2
            )
        },
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onTertiary.copy(alpha = run {
                    val alpha = reversedProgress
                    if (alpha > 0.7F) {
                        alpha
                    } else {
                        0F
                    }
                }),
                maxLines = 1
            )
        },
        navigationIcon = {
            IconButton(onClick = {
                component.onGoBack()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = LocalStringRes.current.back
                )
            }
        },
        actions = {
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null
                )
            }
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null
                )
            }
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Button(onClick = {

                    }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(
                            text = languages.find { it.value.equals(selectedLanguage, true) }?.text ?: "Select Language",
                            maxLines = 1
                        )
                    }
                    Button(onClick = {

                    }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(
                            text = season ?: "Select Season",
                            maxLines = 1
                        )
                    }
                }
            }

            content()
        }
    }
}