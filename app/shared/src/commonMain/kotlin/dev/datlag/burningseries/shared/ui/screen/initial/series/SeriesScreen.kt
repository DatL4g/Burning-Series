package dev.datlag.burningseries.shared.ui.screen.initial.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.diagonalShape
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.ui.custom.Cover
import dev.datlag.burningseries.shared.ui.custom.DefaultCollapsingToolbar
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.datlag.burningseries.shared.ui.screen.initial.series.component.DescriptionText
import dev.datlag.burningseries.shared.ui.screen.initial.series.component.EpisodeItem
import dev.datlag.burningseries.shared.ui.screen.initial.series.component.SeasonAndLanguageButtons
import dev.datlag.burningseries.shared.ui.theme.SchemeTheme
import dev.datlag.burningseries.shared.ui.theme.shape.DiagonalShape
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.abs

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SeriesScreen(component: SeriesComponent) {
    val href by component.commonHref.collectAsStateWithLifecycle()
    val dialogState by component.dialog.subscribeAsState()
    val childState by component.child.subscribeAsState()
    val nextEpisode by component.nextEpisodeToWatch.collectAsStateWithLifecycle(initialValue = null)
    val nextSeason by component.nextSeasonToWatch.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(href) {
        SchemeTheme.setCommon(href)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        childState.child?.also { (_, instance) ->
            instance.render()
        } ?: run {
            when (calculateWindowSizeClass().widthSizeClass) {
                WindowWidthSizeClass.Compact -> CompactScreen(component)
                else -> DefaultScreen(component)
            }
        }

        nextEpisode?.let { next ->
            ExtendedFloatingActionButton(
                onClick = {
                    component.itemClicked(next)
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = next.episodeTitle,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = next.episodeTitle)
            }
        }
        nextSeason?.let { next ->
            ExtendedFloatingActionButton(
                onClick = {
                    component.switchToSeason(next)
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                val seasonText = if (next.title.toIntOrNull() != null) {
                    stringResource(SharedRes.strings.season_placeholder, next.title)
                } else {
                    next.title
                }

                Icon(
                    imageVector = Icons.Default.Redo,
                    contentDescription = next.title,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = seasonText)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            SchemeTheme.setCommon(null)
        }
    }

    dialogState.child?.instance?.render()
    EnterSeriesScreen()
}

@Composable
expect fun EnterSeriesScreen()

@Composable
private fun CompactScreen(component: SeriesComponent) {
    val seriesState by component.seriesState.collectAsStateWithLifecycle()
    val title by component.title.collectAsStateWithLifecycle()
    val coverHref by component.coverHref.collectAsStateWithLifecycle()
    val commonHref by component.commonHref.collectAsStateWithLifecycle()

    DefaultCollapsingToolbar(
        expandedBody = { state ->
            val scope = rememberCoroutineScope()

            Cover(
                key = coverHref,
                data = coverHref?.let { BSUtil.getBurningSeriesLink(it) },
                contentDescription = title.ifBlank {
                    stringResource(SharedRes.strings.loading_intent_series)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 320.dp)
                    .parallax(0.5F)
                    .diagonalShape(-10F, DiagonalShape.POSITION.BOTTOM),
                onSuccess = { success ->
                    SchemeTheme.update(commonHref, success.painter, scope)
                }
            )
            Text(
                text = title.ifBlank {
                    stringResource(SharedRes.strings.loading_intent_series)
                },
                modifier = Modifier.road(Alignment.TopStart, Alignment.BottomStart).padding(16.dp).background(
                    color = Color.Black.copy(alpha = run {
                        val alpha = state.toolbarState.progress
                        if (alpha < 0.5F) {
                            if (alpha < 0.3F) {
                                0F
                            } else {
                                alpha
                            }
                        } else {
                            0.5F
                        }
                    }),
                    shape = MaterialTheme.shapes.small
                ).padding(4.dp),
                color = LocalContentColor.current.copy(alpha = run {
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
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        title = { state ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val reversedProgress by remember {
                    derivedStateOf { (abs(1F - state.toolbarState.progress)) }
                }

                Text(
                    text = title.ifBlank {
                        stringResource(SharedRes.strings.loading_intent_series)
                    },
                    color = LocalContentColor.current.copy(alpha = run {
                        val alpha = reversedProgress
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            }
        },
        navigationIcon = { state ->
            IconButton(
                onClick = {
                    component.goBack()
                },
                modifier = Modifier.background(
                    color = if (state.toolbarState.progress == 1F) Color.Black.copy(alpha = 0.5F) else Color.Black.copy(alpha = state.toolbarState.progress / 10F),
                    shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = stringResource(SharedRes.strings.back)
                )
            }
        },
        actions = { state ->
            val isFavorite by component.isFavorite.collectAsStateWithLifecycle()

            IconButton(
                onClick = {
                    component.toggleFavorite()
                },
                modifier = Modifier.background(
                    color = if (state.toolbarState.progress == 1F) Color.Black.copy(alpha = 0.5F) else Color.Black.copy(alpha = state.toolbarState.progress / 10F),
                    shape = CircleShape
                )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(SharedRes.strings.favorite),
                    tint = if (isFavorite) Color.Red else LocalContentColor.current
                )
            }
        }
    ) {
        when (val current = seriesState) {
            is SeriesState.Loading -> {
                LoadingState(SharedRes.strings.loading_series)
            }
            is SeriesState.Error -> {
                ErrorState(SharedRes.strings.error_loading_series) {
                    component.retryLoadingSeries()
                }
            }
            is SeriesState.Success -> {
                val loadingEpisode by component.loadingEpisodeHref.collectAsStateWithLifecycle()
                val dbEpisodes by component.dbEpisodes.collectAsStateWithLifecycle()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item(key = current.series.description) {
                        DescriptionText(current.series.description)
                    }

                    item(key = listOf(
                        current.series.currentSeason,
                        current.series.currentLanguage,
                        current.series.seasons,
                        current.series.languages
                    )) {
                        SeasonAndLanguageButtons(
                            modifier = Modifier.padding(vertical = 16.dp),
                            selectedSeason = current.series.currentSeason,
                            selectedLanguage = current.series.currentLanguage,
                            seasons = current.series.seasons,
                            languages = current.series.languages,
                            onSeasonClick = { season ->
                                season?.let {
                                    component.showDialog(DialogConfig.Season(it, current.series.seasons))
                                }
                            },
                            onLanguageClick = { language ->
                                language?.let {
                                    component.showDialog(DialogConfig.Language(it, current.series.languages))
                                }
                            }
                        )
                    }

                    SeriesContent(
                        content = current.series,
                        dbEpisodes = dbEpisodes,
                        loadingEpisode = loadingEpisode,
                        onEpisodeClick = {
                            component.itemClicked(it)
                        },
                        onEpisodeLongClick = {
                            component.itemLongClicked(it)
                        },
                        onWatchToggle = { episode, watched ->
                            component.watchToggle(current.series, episode, watched)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultScreen(component: SeriesComponent) {
    val seriesState by component.seriesState.collectAsStateWithLifecycle()
    val title by component.title.collectAsStateWithLifecycle()
    val coverHref by component.coverHref.collectAsStateWithLifecycle()
    val commonHref by component.commonHref.collectAsStateWithLifecycle()

    when (val current = seriesState) {
        is SeriesState.Loading -> {
            LoadingState(SharedRes.strings.loading_series)
        }
        is SeriesState.Error -> {
            ErrorState(SharedRes.strings.error_loading_series) {
                component.retryLoadingSeries()
            }
        }
        is SeriesState.Success -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val loadingEpisode by component.loadingEpisodeHref.collectAsStateWithLifecycle()
                val state = rememberLazyListState(
                    initialFirstVisibleItemIndex = StateSaver.seriesListIndex,
                    initialFirstVisibleItemScrollOffset = StateSaver.seriesListOffset
                )
                val dbEpisodes by component.dbEpisodes.collectAsStateWithLifecycle()

                LazyColumn(
                    state = state,
                    modifier = Modifier.weight(1F),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val scope = rememberCoroutineScope()

                            Column(
                                modifier = Modifier.weight(1F),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = title.ifBlank {
                                                stringResource(SharedRes.strings.loading_intent_series)
                                            },
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            softWrap = true
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(
                                            onClick = {
                                                component.goBack()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBackIosNew,
                                                contentDescription = stringResource(SharedRes.strings.back)
                                            )
                                        }
                                    },
                                    actions = {
                                        val isFavorite by component.isFavorite.collectAsStateWithLifecycle()

                                        IconButton(
                                            onClick = {
                                                component.toggleFavorite()
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = stringResource(SharedRes.strings.favorite),
                                                tint = if (isFavorite) Color.Red else LocalContentColor.current
                                            )
                                        }
                                    }
                                )

                                DescriptionText(current.series.description)

                                SeasonAndLanguageButtons(
                                    selectedSeason = current.series.currentSeason,
                                    selectedLanguage = current.series.currentLanguage,
                                    seasons = current.series.seasons,
                                    languages = current.series.languages,
                                    onSeasonClick = { season ->
                                        season?.let {
                                            component.showDialog(DialogConfig.Season(it, current.series.seasons))
                                        }
                                    },
                                    onLanguageClick = { language ->
                                        language?.let {
                                            component.showDialog(DialogConfig.Language(it, current.series.languages))
                                        }
                                    }
                                )
                            }

                            Cover(
                                key = coverHref,
                                data = coverHref?.let { BSUtil.getBurningSeriesLink(it) },
                                contentDescription = title.ifBlank {
                                    stringResource(SharedRes.strings.loading_intent_series)
                                },
                                modifier = Modifier
                                    .width(200.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .align(Alignment.CenterVertically),
                                onSuccess = { success ->
                                    SchemeTheme.update(commonHref, success.painter, scope)
                                }
                            )
                        }
                    }

                    SeriesContent(
                        content = current.series,
                        dbEpisodes = dbEpisodes,
                        loadingEpisode = loadingEpisode,
                        onEpisodeClick = {
                            component.itemClicked(it)
                        },
                        onEpisodeLongClick = {
                            component.itemLongClicked(it)
                        },
                        onWatchToggle = { episode, watched ->
                            component.watchToggle(current.series, episode, watched)
                        }
                    )
                }
                VerticalScrollbar(rememberScrollbarAdapter(state))

                DisposableEffect(state) {
                    onDispose {
                        StateSaver.seriesListIndex = state.firstVisibleItemIndex
                        StateSaver.seriesListOffset = state.firstVisibleItemScrollOffset
                    }
                }
            }
        }
    }
}

private fun LazyListScope.SeriesContent(
    content: Series,
    dbEpisodes: List<Episode>,
    loadingEpisode: String?,
    onEpisodeClick: (Series.Episode) -> Unit,
    onEpisodeLongClick: (Series.Episode) -> Unit,
    onWatchToggle: (episode: Series.Episode, Boolean) -> Unit
) {
    items(content.episodes, key = { it.href }) { episode ->
        val dbEpisode = remember(dbEpisodes, episode.href) {
            dbEpisodes.firstOrNull { it.href == episode.href } ?: dbEpisodes.firstOrNull { it.href.equals(episode.href, true) }
        }

        EpisodeItem(
            content = episode,
            dbEpisode = dbEpisode,
            isLoading = loadingEpisode.equals(episode.href, true),
            onClick = {
                onEpisodeClick(episode)
            },
            onLongClick = {
                onEpisodeLongClick(episode)
            },
            onWatchToggle = { watched ->
                onWatchToggle(episode, watched)
            }
        )
    }
}