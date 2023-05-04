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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.SemiBlack
import dev.datlag.burningseries.common.focusBorder
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.DefaultValue
import dev.datlag.burningseries.other.StateSaver
import dev.datlag.burningseries.ui.custom.CoverImage
import dev.datlag.burningseries.ui.screen.series.SeriesComponent
import dev.datlag.burningseries.ui.Shape
import dev.datlag.burningseries.ui.screen.series.SeriesLanguageSeasonButtons

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LandscapeToolbar(
    component: SeriesComponent,
    title: String,
    cover: Cover?,
    languages: List<Series.Language>?,
    seasons: List<Series.Season>?,
    selectedLanguage: String?,
    selectedSeason: Series.Season?,
    seasonText: String?,
    linkedSeries: List<Series.Linked>,
    isFavorite: Boolean,
    content: LazyListScope.() -> Unit,
) {
    val state = rememberLazyListState(
        StateSaver.seriesViewPos,
        StateSaver.seriesViewOffset
    )
    val buttonFocusRequester = remember { FocusRequester() }

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
                            modifier = Modifier.width(200.dp),
                            cover = cover,
                            description = title,
                            scale = ContentScale.FillWidth,
                            fallbackIconTint = MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth().focusRequester(buttonFocusRequester),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true
                        )
                        SeriesLanguageSeasonButtons(
                            component,
                            languages,
                            seasons,
                            selectedLanguage,
                            selectedSeason,
                            seasonText
                        )
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
                }, modifier = Modifier.focusBorder(MaterialTheme.colorScheme.onTertiary).background(
                    color = if (state.firstVisibleItemIndex >= 1) Color.Transparent else Color.SemiBlack,
                    shape = Shape.FullRoundedShape
                )) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalStringRes.current.back,
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            },
            title = {
                if (state.firstVisibleItemIndex >= 1) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    component.toggleFavorite()
                }, modifier = Modifier.focusBorder(MaterialTheme.colorScheme.onTertiary).background(
                    color = if (state.firstVisibleItemIndex >= 1) Color.Transparent else Color.SemiBlack,
                    shape = Shape.FullRoundedShape
                ).focusProperties {
                    if (linkedSeries.isEmpty()) {
                        next = buttonFocusRequester
                        end = buttonFocusRequester
                    }
                    down = buttonFocusRequester
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = LocalStringRes.current.favorites,
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiary
                    )
                }
                if (linkedSeries.isNotEmpty()) {
                    IconButton(onClick = {
                        throw IllegalArgumentException("Exception on purpose")
                    }, modifier = Modifier.focusBorder(MaterialTheme.colorScheme.onTertiary).background(
                        color = if (state.firstVisibleItemIndex >= 1) Color.Transparent else Color.SemiBlack,
                        shape = Shape.FullRoundedShape
                    ).focusProperties {
                        next = buttonFocusRequester
                        end = buttonFocusRequester
                        down = buttonFocusRequester
                    }) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = LocalStringRes.current.linkedSeries,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
        )
    }

    DisposableEffect(state) {
        onDispose {
            StateSaver.seriesViewPos = state.firstVisibleItemIndex
            StateSaver.seriesViewOffset = state.firstVisibleItemScrollOffset
        }
    }
}