package dev.datlag.burningseries.ui.screen.initial.series

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import dev.datlag.burningseries.ui.screen.initial.series.component.SeasonAndLanguageButtons
import dev.icerock.moko.resources.compose.stringResource
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource
import kotlin.math.abs

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SeriesScreen(component: SeriesComponent) {
    when (calculateWindowSizeClass().widthSizeClass) {
        WindowWidthSizeClass.Compact -> CompactScreen(component)
        else -> DefaultScreen(component)
    }
}

@Composable
private fun CompactScreen(component: SeriesComponent) {
    val seriesState by component.seriesState.collectAsStateWithLifecycle()

    DefaultCollapsingToolbar(
        expandedBody = { state ->
            when (val resource = asyncPainterResource(component.initialCoverHref?.let { BSUtil.getBurningSeriesLink(it) } ?: String())) {
                is Resource.Loading, is Resource.Failure -> { }
                is Resource.Success -> {
                    Image(
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 320.dp).parallax(0.5F),
                        painter = resource.value,
                        contentDescription = component.initialTitle,
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }

            Text(
                text = component.initialTitle,
                modifier = Modifier.road(Alignment.TopStart, Alignment.BottomStart).padding(16.dp),
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
            val reversedProgress by remember {
                derivedStateOf { (abs(1F - state.toolbarState.progress)) }
            }
            Text(
                text = component.initialTitle,
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
        },
        navigationIcon = {

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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item(
                        key = {
                            listOf(
                                current.series.seasons,
                                current.series.languages
                            )
                        }
                    ) {
                        SeasonAndLanguageButtons(
                            current.series.currentSeason,
                            current.series.currentLanguage,
                            current.series.seasons,
                            current.series.languages,
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

@Composable
private fun DefaultScreen(component: SeriesComponent) {
    val seriesState by component.seriesState.collectAsStateWithLifecycle()

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
                item(
                    key = {
                        listOf(
                            current.series.seasons,
                            current.series.languages
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (val resource = asyncPainterResource(component.initialCoverHref?.let { BSUtil.getBurningSeriesLink(it) } ?: String())) {
                            is Resource.Loading, is Resource.Failure -> { }
                            is Resource.Success -> {
                                Image(
                                    modifier = Modifier.width(200.dp).clip(MaterialTheme.shapes.medium),
                                    painter = resource.value,
                                    contentDescription = component.initialTitle,
                                    contentScale = ContentScale.FillWidth,
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = component.initialTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true
                            )

                            SeasonAndLanguageButtons(
                                current.series.currentSeason,
                                current.series.currentLanguage,
                                current.series.seasons,
                                current.series.languages,
                                onSeasonClick = { },
                                onLanguageClick = { }
                            )
                        }
                    }
                }

                SeriesContent(current.series)
            }
        }
    }
}

private fun LazyListScope.SeriesContent(content: Series) {
    item {
        var expanded by remember { mutableStateOf(false) }

        ReadMoreText(
            text = content.description,
            expanded = expanded,
            onExpandedChange = {
                expanded = it
            },
            modifier = Modifier.fillMaxWidth(),
            readMoreText = stringResource(SharedRes.strings.read_more),
            readMoreColor = MaterialTheme.colorScheme.primary,
            readMoreFontWeight = FontWeight.SemiBold,
            readMoreMaxLines = 2,
            readMoreOverflow = ReadMoreTextOverflow.Ellipsis,
            readLessText = stringResource(SharedRes.strings.read_less),
            readLessColor = MaterialTheme.colorScheme.primary,
            readLessFontWeight = FontWeight.SemiBold,
            toggleArea = ToggleArea.All
        )
    }
}