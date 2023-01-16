package dev.datlag.burningseries.ui.screen.home.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import dev.datlag.burningseries.common.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.cardItemSize
import dev.datlag.burningseries.common.coverFileName
import dev.datlag.burningseries.common.isTv
import dev.datlag.burningseries.database.DBSeries
import dev.datlag.burningseries.database.SelectLatestEpisodesAmount
import dev.datlag.burningseries.model.Cover
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.ui.custom.CoverImage
import dev.datlag.burningseries.ui.screen.home.LocalFabGroupRequester
import java.io.File

@Composable
fun EpisodeItem(content: Home.Episode, component: EpisodesComponent) {
    val (series, episode) = remember { content.getSeriesAndEpisode() }

    EpisodeItem(
        content.title,
        content.cover,
        content.href,
        series,
        episode,
        false,
        component
    )
}

@Composable
fun EpisodeItem(content: SelectLatestEpisodesAmount, component: EpisodesComponent) {
    val base64 = remember { File(component.imageDir, DBSeries(
        content.hrefTitle,
        content.href,
        content.title,
        content.coverHref,
        content.favoriteSince
    ).coverFileName()).readText() }

    EpisodeItem(
        content.episodeTitle,
        Cover(
            href = content.coverHref ?: String(),
            base64 = base64
        ),
        content.episodeHref,
        content.title,
        content.episodeTitle,
        true,
        component
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeItem(
    title: String,
    cover: Cover,
    href: String,
    series: String,
    episode: String,
    continueWatching: Boolean,
    component: EpisodesComponent
) {
    val fabGroup = LocalFabGroupRequester.current

    Card(modifier = Modifier.cardItemSize().onClick {
        component.onEpisodeClicked(href, SeriesInitialInfo(series, cover), continueWatching)
    }.focusProperties {
        if (fabGroup != null) {
            down = fabGroup
        }
    }) {
        CoverImage(
            cover = cover,
            description = title,
            scale = ContentScale.FillBounds,
            fallbackIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.cardItemSize().height(300.dp)
        )
        Text(
            text = series,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            modifier = Modifier.cardItemSize().padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Text(
            text = episode,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            modifier = Modifier.cardItemSize().padding(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 8.dp)
        )
    }
}