package dev.datlag.burningseries.ui.screen.series.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.SemiBlack
import dev.datlag.burningseries.other.DefaultValue
import dev.datlag.burningseries.ui.custom.CoverImage
import dev.datlag.burningseries.ui.screen.series.SeriesComponent
import dev.datlag.burningseries.ui.Shape

@Composable
fun LandscapeToolbar(
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

    val state = rememberLazyListState()

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cover != null) {
                        CoverImage(
                            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
                            cover = cover,
                            description = title,
                            scale = ContentScale.FillWidth,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2
                        )
                        Button(onClick = {

                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = languages.find { it.value.equals(selectedLanguage, true) }?.text ?: "Select Language",
                                maxLines = 1
                            )
                        }
                        Button(onClick = {

                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = season ?: "Select Season",
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            content()
        }

        TopAppBar(
            backgroundColor = if (state.firstVisibleItemIndex >= 1) MaterialTheme.colorScheme.tertiary else Color.Transparent,
            contentColor = if (state.firstVisibleItemIndex >= 1) MaterialTheme.colorScheme.onTertiary else Color.Transparent,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = {
                    component.onGoBack()
                }, modifier = Modifier.background(
                    color = if (state.firstVisibleItemIndex >= 1) Color.Transparent else Color.SemiBlack,
                    shape = Shape.FullRoundedShape
                )) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalStringRes.current.back
                    )
                }
            },
            title = {
                if (state.firstVisibleItemIndex >= 1) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onTertiary,
                        maxLines = 1
                    )
                }
            },
            actions = {
                IconButton(onClick = {

                }, modifier = Modifier.background(
                    color = if (state.firstVisibleItemIndex >= 1) Color.Transparent else Color.SemiBlack,
                    shape = Shape.FullRoundedShape
                )) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null
                    )
                }
                IconButton(onClick = {

                }, modifier = Modifier.background(
                    color = if (state.firstVisibleItemIndex >= 1) Color.Transparent else Color.SemiBlack,
                    shape = Shape.FullRoundedShape
                )) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null
                    )
                }
            }
        )
    }
}