package dev.datlag.burningseries.ui.screen.series

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.OnWarning
import dev.datlag.burningseries.common.Warning
import dev.datlag.burningseries.common.fillWidthInPortraitMode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.ui.custom.readmoretext.ReadMoreText
import dev.datlag.burningseries.ui.screen.series.toolbar.LandscapeToolbar
import dev.datlag.burningseries.ui.screen.series.toolbar.PortraitToolbar
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.model.common.maxSize
import dev.datlag.burningseries.network.Status
import dev.datlag.burningseries.ui.Shape
import dev.datlag.burningseries.ui.custom.ChipGroup
import dev.datlag.burningseries.ui.dialog.language.LanguageComponent
import dev.datlag.burningseries.ui.dialog.language.LanguageDialog
import dev.datlag.burningseries.ui.custom.InfoCard
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
    val description by component.description.collectAsState(String())
    val episodes by component.episodes.collectAsState(emptyList())

    val _title by component.title.collectAsState(component.initialInfo.title)
    val title = _title ?: component.initialInfo.title
    val _cover by component.cover.collectAsState(component.initialInfo.cover)
    val cover = _cover ?: component.initialInfo.cover

    val selectedLanguage by component.selectedLanguage.collectAsState(null)
    val languages by component.languages.collectAsState(null)
    val seasons by component.seasons.collectAsState(null)
    val seasonText by component.seasonText.collectAsState(null)
    val selectedSeason by component.selectedSeason.collectAsState(null)

    val genreInfo by component.genreInfo.collectAsState(null)
    val genres = genreInfo?.data?.trim()?.split("\\s".toRegex())?.maxSize(5) ?: emptyList()

    val _additionalInfo by component.additionalInfo.collectAsState(null)
    val additionalInfo = _additionalInfo ?: emptyList()

    val linkedSeries by component.linkedSeries.collectAsState(emptyList())
    val isFavorite by component.isFavorite.collectAsState(false)

    val continueEpisode by component.continueEpisode.collectAsState(null)
    val hosterSorted by component.hosterSorted.collectAsState(false)

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
                    episodes,
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
    episodes: List<Series.Episode>
) {
    item {
        ReadMoreText(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            text = description ?: String(),
            expanded = false,
            readMoreText = LocalStringRes.current.readMore
        )
    }

    item {
        ChipGroup(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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

    items(episodes) { episode ->
        val isEnabled = remember { episode.hoster.isNotEmpty() }
        Row(
            modifier = Modifier.fillMaxWidth().onClick(enabled = isEnabled, onLongClick = {
                component.showDialog(DialogConfig.Activate(episode))
            }) {
                component.loadEpisode(episode)
            }.padding(16.dp).alpha(if (isEnabled) 0.5F else 1F),
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
            Icon(
                imageVector = watchIcon,
                contentDescription = null,
                tint = watchTint
            )
        }
    }
}
