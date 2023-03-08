package dev.datlag.burningseries.ui.screen.series

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.ui.screen.series.toolbar.LandscapeToolbar
import dev.datlag.burningseries.ui.screen.series.toolbar.PortraitToolbar
import dev.datlag.burningseries.model.common.maxSize
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.Shape
import dev.datlag.burningseries.ui.custom.ChipGroup
import dev.datlag.burningseries.ui.dialog.language.LanguageComponent
import dev.datlag.burningseries.ui.dialog.language.LanguageDialog
import dev.datlag.burningseries.ui.custom.InfoCard
import dev.datlag.burningseries.ui.custom.readmoretext.ReadMoreText
import dev.datlag.burningseries.ui.custom.readmoretext.ReadMoreTextOverflow
import dev.datlag.burningseries.ui.custom.readmoretext.ToggleArea
import dev.datlag.burningseries.ui.custom.snackbarHandlerForStatus
import dev.datlag.burningseries.ui.dialog.activate.ActivateComponent
import dev.datlag.burningseries.ui.dialog.activate.ActivateDialog
import dev.datlag.burningseries.ui.dialog.nostream.NoStreamComponent
import dev.datlag.burningseries.ui.dialog.nostream.NoStreamDialog
import dev.datlag.burningseries.ui.dialog.season.SeasonComponent
import dev.datlag.burningseries.ui.dialog.season.SeasonDialog
import kotlinx.coroutines.flow.consumeAsFlow


@Composable
fun SeriesScreen(component: SeriesComponent) {
    val dialogState = component.dialog.subscribeAsState()
    val description by component.description.collectAsStateSafe { String() }
    val episodes by component.episodes.collectAsStateSafe { emptyList() }

    val _title by component.title.collectAsStateSafe { component.initialInfo.title }
    val title = _title ?: component.initialInfo.title
    val _cover by component.cover.collectAsStateSafe { component.initialInfo.cover }
    val cover = _cover ?: component.initialInfo.cover

    val selectedLanguage by component.selectedLanguage.collectAsStateSafe { null }
    val languages by component.languages.collectAsStateSafe { null }
    val seasons by component.seasons.collectAsStateSafe { null }
    val seasonText by component.seasonText.collectAsStateSafe { null }
    val selectedSeason by component.selectedSeason.collectAsStateSafe { null }

    val genreInfo by component.genreInfo.collectAsStateSafe { null }
    val genres = genreInfo?.data?.trim()?.split("\\s".toRegex())?.maxSize(5) ?: emptyList()

    val _additionalInfo by component.additionalInfo.collectAsStateSafe { null }
    val additionalInfo = _additionalInfo ?: emptyList()

    val linkedSeries by component.linkedSeries.collectAsStateSafe { emptyList() }
    val isFavorite by component.isFavorite.collectAsStateSafe { false }

    val continueEpisode by component.continueEpisode.collectAsStateSafe { null }
    val hosterSorted by component.hosterSorted.collectAsStateSafe { false }

    val isPortrait = LocalOrientation.current is Orientation.PORTRAIT

    Box(modifier = Modifier.fillMaxSize()) {
        when (LocalOrientation.current) {
            is Orientation.PORTRAIT -> PortraitToolbar(
                component,
                title,
                cover,
                languages,
                seasons,
                selectedLanguage,
                selectedSeason,
                seasonText,
                linkedSeries,
                isFavorite
            ) {
                SeriesScreenContent(
                    component,
                    description,
                    genres,
                    additionalInfo,
                    hosterSorted,
                    isPortrait,
                    episodes
                )
            }

            is Orientation.LANDSCAPE -> LandscapeToolbar(
                component,
                title,
                cover,
                languages,
                seasons,
                selectedLanguage,
                selectedSeason,
                seasonText,
                linkedSeries,
                isFavorite
            ) {
                SeriesScreenContent(
                    component,
                    description,
                    genres,
                    additionalInfo,
                    hosterSorted,
                    isPortrait,
                    episodes
                )
            }
        }

        if (continueEpisode != null) {
            ExtendedFloatingActionButton(onClick = {
                component.loadEpisode(continueEpisode!!)
            }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = (if (continueEpisode!!.watchPosition > 0L) {
                        LocalStringRes.current.continueEpisode
                    } else {
                        LocalStringRes.current.startEpisode
                    }).format(continueEpisode?.episodeNumberOrListNumber?.toString() ?: String()),
                    maxLines = 1
                )
            }
        }
    }

    dialogState.value.overlay?.also { (config, instance) ->
        when (config) {
            is DialogConfig.Language -> {
                LanguageDialog(instance as LanguageComponent)
            }
            is DialogConfig.Season -> {
                SeasonDialog(instance as SeasonComponent)
            }
            is DialogConfig.NoStream -> {
                NoStreamDialog(instance as NoStreamComponent)
            }
            is DialogConfig.Activate -> {
                ActivateDialog(instance as ActivateComponent)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun LazyListScope.SeriesScreenContent(
    component: SeriesComponent,
    description: String?,
    genres: List<String>,
    additionalInfo: List<Series.Info>,
    hosterSorted: Boolean,
    isPortrait: Boolean,
    episodes: List<Series.Episode>
) {
    item {
        val (expand, onExpandChange) = rememberSaveable { mutableStateOf(false) }

        if (!description.isNullOrEmpty()) {
            ReadMoreText(
                modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                text = description,
                expanded = expand,
                onExpandedChange = onExpandChange,
                readMoreText = LocalStringRes.current.readMore,
                readMoreColor = MaterialTheme.colorScheme.primary,
                readLessText = LocalStringRes.current.readLess,
                readMoreOverflow = ReadMoreTextOverflow.Ellipsis,
                toggleArea = ToggleArea.More
            )
        }
    }

    item {
        ChipGroup(
            modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp),
            horizontalSpace = 8.dp,
            verticalAlignment = Alignment.CenterVertically
        ) {
            genres.forEach {
                Chip(onClick = {

                }) {
                    Text(text = it)
                }
            }
        }
    }

    items(additionalInfo) { info ->
        Row(
            modifier = Modifier.fillParentMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1F),
                text = info.header.trim(),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                modifier = Modifier.weight(2F),
                text = info.trimmedData(),
                maxLines = 1
            )
        }
    }

    if (!hosterSorted) {
        item {
            Box(
                modifier = Modifier.fillParentMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                InfoCard(
                    text = LocalStringRes.current.sortHosterHint,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.onClick {
                        component.onSettingsClicked()
                    }.fillWidthInPortraitMode()
                )
            }
        }
    }

    if (
        isPortrait && episodes.any {
            it.isCanon != null || it.isFiller != null
        }
    ) {
        item {
            Column(
                modifier = Modifier.fillParentMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                val strings = LocalStringRes.current
                val canonColor = Color.Success
                val canonText = strings.canon
                val mixedColor = Color.Warning
                val mixedText = strings.mixed
                val fillerColor = MaterialTheme.colorScheme.error
                val fillerText = strings.filler

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Chip(
                        onClick = {},
                        shape = Shape.FullRoundedShape,
                        colors = ChipDefaults.outlinedChipColors(
                            contentColor = canonColor
                        ),
                        border = BorderStroke(1.dp, canonColor)
                    ) {
                        Text(
                            text = canonText.first().toString(),
                            color = canonColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = canonText
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Chip(
                        onClick = {},
                        shape = Shape.FullRoundedShape,
                        colors = ChipDefaults.outlinedChipColors(
                            contentColor = mixedColor
                        ),
                        border = BorderStroke(1.dp, mixedColor)
                    ) {
                        Text(
                            text = mixedText.first().toString(),
                            color = mixedColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = mixedText
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Chip(
                        onClick = {},
                        shape = Shape.FullRoundedShape,
                        colors = ChipDefaults.outlinedChipColors(
                            contentColor = fillerColor
                        ),
                        border = BorderStroke(1.dp, fillerColor)
                    ) {
                        Text(
                            text = fillerText.first().toString(),
                            color = fillerColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = fillerText
                    )
                }
            }
        }
    }

    items(episodes) { episode ->
        val strings = LocalStringRes.current
        val isEnabled = remember(episode.href) { episode.hoster.isNotEmpty() }
        val colorScheme = MaterialTheme.colorScheme
        val (color, text) = remember(episode.href) {
            if (episode.isCanon == true && episode.isFiller == true) {
                Color.Warning to strings.mixed
            } else if (episode.isCanon == true) {
                Color.Success to strings.canon
            } else if (episode.isFiller == true) {
                colorScheme.error to strings.filler
            } else {
                null to null
            }
        }
        val chipText = if (isPortrait) {
            text?.first()?.toString()
        } else {
            text
        }

        Row(
            modifier = Modifier.fillParentMaxWidth().onClick(enabled = isEnabled, onLongClick = {
                component.showDialog(DialogConfig.Activate(episode))
            }) {
                component.loadEpisode(episode)
            }.padding(16.dp).alpha(if (isEnabled) 1F else 0.5F),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val watchState = episode.getWatchState()
            val (watchIcon, watchTint) = when (watchState) {
                is Series.Episode.WatchState.NONE -> Icons.Default.Clear to Color.Transparent
                is Series.Episode.WatchState.STARTED -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.onBackground
                is Series.Episode.WatchState.FINISHED -> Icons.Default.Check to MaterialTheme.colorScheme.onBackground
            }

            Text(
                text = episode.number,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.weight(1F),
                text = episode.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
            if (color != null && chipText != null) {
                Chip(
                    onClick = {},
                    shape = Shape.FullRoundedShape,
                    colors = ChipDefaults.outlinedChipColors(
                        contentColor = color
                    ),
                    border = BorderStroke(1.dp, color)
                ) {
                    Text(
                        text = chipText,
                        color = color,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = watchIcon,
                contentDescription = null,
                tint = watchTint
            )
        }
    }
}
