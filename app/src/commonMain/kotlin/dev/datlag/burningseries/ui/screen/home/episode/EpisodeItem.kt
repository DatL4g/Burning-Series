package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import dev.datlag.burningseries.common.onClick
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.custom.CoverImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeItem(content: Home.Episode, component: EpisodesComponent) {
    val (series, episode) = remember { content.getSeriesAndEpisode() }

    Card(modifier = Modifier.fillMaxWidth().onClick {
        component.onEpisodeClicked(content.href, SeriesInitialInfo(series, content.cover))
    }) {
        CoverImage(
            cover = content.cover,
            description = content.title,
            scale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
        Text(
            text = series,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Text(
            text = episode,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 8.dp)
        )
    }
}