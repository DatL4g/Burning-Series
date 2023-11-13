package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.diagonalShape
import dev.datlag.burningseries.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.state.SeriesState
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.ui.custom.CountryImage
import dev.datlag.burningseries.ui.custom.DefaultCollapsingToolbar
import dev.datlag.burningseries.ui.custom.readmore.ReadMoreText
import dev.datlag.burningseries.ui.custom.readmore.ReadMoreTextOverflow
import dev.datlag.burningseries.ui.custom.readmore.ToggleArea
import dev.datlag.burningseries.ui.custom.state.ErrorState
import dev.datlag.burningseries.ui.custom.state.LoadingState
import dev.datlag.burningseries.ui.screen.initial.series.component.DescriptionText
import dev.datlag.burningseries.ui.screen.initial.series.component.EpisodeItem
import dev.datlag.burningseries.ui.screen.initial.series.component.SeasonAndLanguageButtons
import dev.datlag.burningseries.ui.theme.CommonSchemeTheme
import dev.datlag.burningseries.ui.theme.SchemeTheme
import dev.datlag.burningseries.ui.theme.loadImageScheme
import dev.datlag.burningseries.ui.theme.shape.DiagonalShape
import dev.icerock.moko.resources.compose.stringResource
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlin.math.abs

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SeriesScreen(component: SeriesComponent) {
    val href by component.href.collectAsStateWithLifecycle()

    SchemeTheme.setCommon(href)
    when (calculateWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactScreen(component)
        else -> DefaultScreen(component)
    }

    val scope = rememberCoroutineScope()
    DisposableEffect(scope) {
        onDispose {
            SchemeTheme.setCommon(null, scope)
        }
    }
}

@Composable
private fun CompactScreen(component: SeriesComponent) {
    val seriesState by component.seriesState.collectAsStateWithLifecycle()
    val title by component.title.collectAsStateWithLifecycle()
    val href by component.href.collectAsStateWithLifecycle()
    val coverHref by component.coverHref.collectAsStateWithLifecycle()

    DefaultCollapsingToolbar(
        expandedBody = { state ->
            when (val resource = asyncPainterResource(coverHref?.let { BSUtil.getBurningSeriesLink(it) } ?: String())) {
                is Resource.Loading, is Resource.Failure -> { }
                is Resource.Success -> {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 320.dp)
                            .parallax(0.5F)
                            .diagonalShape(-10F, DiagonalShape.POSITION.BOTTOM),
                        painter = resource.value,
                        contentDescription = title,
                        contentScale = ContentScale.FillWidth,
                    )
                    loadImageScheme(href, resource.value)
                }
            }

            Text(
                text = title,
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
                    text = title,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        DescriptionText(current.series.description)
                    }

                    item {
                        SeasonAndLanguageButtons(
                            modifier = Modifier.padding(vertical = 16.dp),
                            selectedSeason = current.series.currentSeason,
                            selectedLanguage = current.series.currentLanguage,
                            seasons = current.series.seasons,
                            languages = current.series.languages,
                            onSeasonClick = { },
                            onLanguageClick = { }
                        )
                    }

                    SeriesContent(current.series)
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
    val href by component.href.collectAsStateWithLifecycle()
    val coverHref by component.coverHref.collectAsStateWithLifecycle()

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
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1F),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = title,
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
                                }
                            )

                            DescriptionText(current.series.description)

                            SeasonAndLanguageButtons(
                                selectedSeason = current.series.currentSeason,
                                selectedLanguage = current.series.currentLanguage,
                                seasons = current.series.seasons,
                                languages = current.series.languages,
                                onSeasonClick = { },
                                onLanguageClick = { }
                            )
                        }

                        when (val resource = asyncPainterResource(coverHref?.let { BSUtil.getBurningSeriesLink(it) } ?: String())) {
                            is Resource.Loading, is Resource.Failure -> { }
                            is Resource.Success -> {
                                Image(
                                    modifier = Modifier.width(200.dp).clip(MaterialTheme.shapes.medium).align(Alignment.CenterVertically),
                                    painter = resource.value,
                                    contentDescription = title,
                                    contentScale = ContentScale.FillWidth,
                                )
                                loadImageScheme(BSUtil.fixSeriesHref(href), resource.value)
                            }
                        }
                    }
                }

                SeriesContent(current.series)
            }
        }
    }
}

private fun LazyListScope.SeriesContent(content: Series) {
    items(content.episodes) { episode ->
        EpisodeItem(episode)
    }
}