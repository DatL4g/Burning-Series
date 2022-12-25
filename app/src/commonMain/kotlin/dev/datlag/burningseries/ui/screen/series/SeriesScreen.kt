package dev.datlag.burningseries.ui.screen.series

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.ui.custom.readmoretext.ReadMoreText
import dev.datlag.burningseries.ui.screen.series.toolbar.LandscapeToolbar
import dev.datlag.burningseries.ui.screen.series.toolbar.PortraitToolbar
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.model.common.maxSize
import dev.datlag.burningseries.ui.dialog.language.LanguageComponent
import dev.datlag.burningseries.ui.dialog.language.LanguageDialog
import dev.datlag.burningseries.other.Logger
import dev.datlag.burningseries.ui.dialog.nostream.NoStreamComponent
import dev.datlag.burningseries.ui.dialog.nostream.NoStreamDialog
import dev.datlag.burningseries.ui.dialog.season.SeasonComponent
import dev.datlag.burningseries.ui.dialog.season.SeasonDialog


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

    var continueEpisode = episodes.findLast { it.watchPosition > 0L } ?: episodes.firstOrNull()
    if (continueEpisode?.isFinished == true) {
        val finishedIndex = episodes.indexOf(continueEpisode)
        if (episodes.size > finishedIndex) {
            continueEpisode = episodes[finishedIndex + 1]
        }
    }

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
                    episodes
                )
            }
        }

        if (continueEpisode != null) {
            ExtendedFloatingActionButton(onClick = {
                component.loadEpisode(continueEpisode)
            }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    text = if (continueEpisode.watchPosition > 0L) {
                        "Continue [Ep. ${continueEpisode.episodeNumberOrListNumber?.toString() ?: String()}]"
                    } else {
                        "Start [Ep. ${continueEpisode.episodeNumberOrListNumber?.toString() ?: String()}]"
                    },
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
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun LazyListScope.SeriesScreenContent(
    component: SeriesComponent,
    description: String?,
    genres: List<String>,
    additionalInfo: List<Series.Info>,
    episodes: List<Series.Episode>
) {
    item {
        ReadMoreText(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            text = description ?: String(),
            expanded = false,
            readMoreText = "Read More"
        )
    }

    item {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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

    items(episodes) { episode ->
        Row(
            modifier = Modifier.fillMaxWidth().onClick {
                component.loadEpisode(episode)
            }.padding(16.dp),
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
                maxLines = 1
            )
            Icon(
                imageVector = watchIcon,
                contentDescription = null,
                tint = watchTint
            )
        }
    }
}
