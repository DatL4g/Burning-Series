package dev.datlag.burningseries.ui.screen.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalOrientation
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

    val genreInfo by component.genreInfo.collectAsState(null)
    val genres = genreInfo?.data?.trim()?.split("\\s".toRegex())?.maxSize(5) ?: emptyList()

    val _additionalInfo by component.additionalInfo.collectAsState(null)
    val additionalInfo = _additionalInfo ?: emptyList()

    when (LocalOrientation.current) {
        is Orientation.PORTRAIT -> PortraitToolbar(
            component,
            title,
            cover,
            languages,
            seasons,
            selectedLanguage,
            seasonText
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
            seasonText
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
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                tint = Color.Transparent
            )
        }
    }
}
