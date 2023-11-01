package dev.datlag.burningseries.ui.screen.initial.home.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.ui.theme.CountryImage
import dev.icerock.moko.resources.compose.painterResource
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.EpisodeItem(episode: Home.Episode, onclick: () -> Unit) {
    ElevatedCard(modifier = Modifier.animateItemPlacement().height(200.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CardDefaults.elevatedShape
            ) {
                episode.coverHref?.let { cover ->
                    when (val resource = asyncPainterResource(BSUtil.getBurningSeriesLink(cover))) {
                        is Resource.Loading, is Resource.Failure -> {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1F, true)
                                    .clip(CardDefaults.elevatedShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                            )
                        }
                        is Resource.Success -> {
                            Image(
                                painter = resource.value,
                                contentScale = ContentScale.FillWidth,
                                contentDescription = episode.title,
                                modifier = Modifier.aspectRatio(1F, true)
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = episode.series ?: episode.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                    maxLines = 2
                )
                if (episode.series != null) {
                    Text(
                        text = episode.episode ?: episode.title,
                        modifier = Modifier.padding(bottom = 8.dp),
                        maxLines = 3
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val code = remember(episode.href) { episode.flags.firstOrNull()?.bestCountryCode }
                    if (code != null) {
                        Image(
                            painter = painterResource(CountryImage.getByCode(code)),
                            contentDescription = episode.flags.firstOrNull()?.title,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .border(1.dp, LocalContentColor.current, MaterialTheme.shapes.extraSmall)
                        )
                    }
                    Text(text = episode.info)
                }
            }
        }
    }
}