package dev.datlag.burningseries.shared.ui.screen.initial.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.haze
import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.shared.LocalHaze
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.LocalPadding
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.localPadding
import dev.datlag.burningseries.shared.other.StateSaver
import dev.datlag.burningseries.shared.ui.custom.Cover
import dev.datlag.burningseries.shared.ui.custom.VerticalScrollbar
import dev.datlag.burningseries.shared.ui.custom.rememberScrollbarAdapter
import dev.datlag.burningseries.shared.ui.custom.state.ErrorState
import dev.datlag.burningseries.shared.ui.custom.state.LoadingState
import dev.datlag.burningseries.shared.ui.screen.initial.series.component.DescriptionText
import dev.datlag.burningseries.shared.ui.screen.initial.series.component.EpisodeItem
import dev.datlag.burningseries.shared.ui.screen.initial.series.component.SeasonAndLanguageButtons
import dev.datlag.burningseries.shared.ui.theme.SchemeTheme
import dev.datlag.burningseries.shared.ui.theme.TopLeftBottomRightRoundedShape
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SeriesScreen(component: SeriesComponent) {
    val href by component.commonHref.collectAsStateWithLifecycle()
    val dialogState by component.dialog.subscribeAsState()
    val childState by component.child.subscribeAsState()

    LaunchedEffect(href) {
        SchemeTheme.setCommon(href)
    }

    childState.child?.instance?.render() ?: run {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val loadingEpisode by component.loadingEpisodeHref.collectAsStateWithLifecycle()
            val nextEpisode by component.nextEpisodeToWatch.collectAsStateWithLifecycle(initialValue = null)
            val nextSeason by component.nextSeasonToWatch.collectAsStateWithLifecycle(initialValue = null)
            val availableEpisode = if (nextEpisode?.hosters?.isNotEmpty() == true) {
                nextEpisode
            } else {
                null
            }

            DefaultScreen(component, loadingEpisode)

            availableEpisode?.let { next ->
                ExtendedFloatingActionButton(
                    onClick = {
                        component.itemClicked(next)
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).localPadding(16.dp)
                ) {
                    if (loadingEpisode.equals(next.href, ignoreCase = true)) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = next.episodeTitle,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = next.episodeTitle,
                        maxLines = 2
                    )
                }
            }
            nextSeason?.let { next ->
                ExtendedFloatingActionButton(
                    onClick = {
                        component.switchToSeason(next)
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).localPadding(16.dp)
                ) {
                    val seasonText = if (next.title.toIntOrNull() != null) {
                        stringResource(SharedRes.strings.season_placeholder, next.title)
                    } else {
                        next.title
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Redo,
                        contentDescription = next.title,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = seasonText)
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultScreen(component: SeriesComponent, loadingEpisode: String?) {
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
                val state = rememberLazyListState(
                    initialFirstVisibleItemIndex = StateSaver.seriesListIndex,
                    initialFirstVisibleItemScrollOffset = StateSaver.seriesListOffset
                )
                val dbEpisodes by component.dbEpisodes.collectAsStateWithLifecycle()

                LazyColumn(
                    state = state,
                    modifier = Modifier.weight(1F).haze(state = LocalHaze.current),
                    contentPadding = LocalPadding(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .align(Alignment.CenterVertically)
                            ) {
                                val scope = rememberCoroutineScope()

                                Cover(
                                    modifier = Modifier.fillMaxSize(),
                                    key = coverHref,
                                    data = coverHref?.let { BSUtil.getBurningSeriesLink(it) },
                                    contentDescription = title.ifBlank {
                                        stringResource(SharedRes.strings.loading_intent_series)
                                    },
                                    onSuccess = { success ->
                                        SchemeTheme.update(commonHref, success.painter, scope)
                                    }
                                )
                                IconButton(
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5F), shape = TopLeftBottomRightRoundedShape(
                                        RoundedCornerShape(12.dp)
                                    )),
                                    onClick = {
                                        component.goBack()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBackIosNew,
                                        contentDescription = stringResource(SharedRes.strings.back),
                                        tint = Color.White
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1F),
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                            ) {
                                val subTitle by component.subTitle.collectAsStateWithLifecycle()
                                val isFavorite by component.isFavorite.collectAsStateWithLifecycle()

                                IconButton(
                                    modifier = Modifier.align(Alignment.End),
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
                                Spacer(Modifier.weight(1F))
                                Text(
                                    text = title.ifBlank {
                                        stringResource(SharedRes.strings.loading_intent_series)
                                    },
                                    maxLines = 2,
                                    fontWeight = FontWeight.SemiBold,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = true,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                subTitle?.let {
                                    Text(
                                        text = it,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = true
                                    )
                                }
                                Spacer(Modifier.weight(1F))
                            }
                        }
                    }

                    item {
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