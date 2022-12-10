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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import dev.datlag.burningseries.LocalOrientation
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.Orientation
import dev.datlag.burningseries.ui.custom.readmoretext.ReadMoreText
import dev.datlag.burningseries.ui.screen.series.toolbar.LandscapeToolbar
import dev.datlag.burningseries.ui.screen.series.toolbar.PortraitToolbar
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.ui.dialog.language.LanguageComponent
import dev.datlag.burningseries.ui.dialog.language.LanguageDialog
import dev.datlag.burningseries.other.Logger


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
            seasonText
        ) {
            SeriesScreenContent(
                component,
                description,
                episodes
            )
        }
    }

    dialogState.value.overlay?.also { (config, instance) ->
        Logger.error("Display dialog")
        when (config) {
            is DialogConfig.Language -> {
                Logger.error("Display language dialog")
                LanguageDialog(instance as LanguageComponent)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun LazyListScope.SeriesScreenContent(
    component: SeriesComponent,
    description: String?,
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
            Chip(onClick = {

            }) {
                Text(text = "Chip 1")
            }

            Chip(onClick = {

            }) {
                Text(text = "Chip 2")
            }

            Chip(onClick = {

            }) {
                Text(text = "Chip 3")
            }
        }
    }

    items(episodes) { episode ->
        Row(
            modifier = Modifier.fillMaxWidth().onClick {
                // load episode here
                println("Clicked: ${episode.title}")
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
