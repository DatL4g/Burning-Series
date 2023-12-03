package dev.datlag.burningseries.shared.ui.screen.initial.home.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.shared.common.bounceClick
import dev.datlag.burningseries.shared.common.focusScale
import dev.datlag.burningseries.shared.common.onClick
import dev.datlag.burningseries.shared.ui.custom.AutoSizeText
import dev.datlag.burningseries.shared.ui.custom.CountryImage
import io.kamel.core.Resource
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.EpisodeItem(episode: Home.Episode, onclick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.animateItemPlacement().focusScale(1.02F).height(150.dp).bounceClick().clip(MaterialTheme.shapes.medium).onClick {
            onclick()
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CardDefaults.elevatedShape
            ) {
                when (val resource = asyncPainterResource(episode.coverHref?.let { BSUtil.getBurningSeriesLink(it) } ?: String())) {
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
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = episode.series ?: episode.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
                if (episode.series != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1F),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AutoSizeText(
                            text = episode.episode ?: episode.title,
                            maxLines = 2,
                            softWrap = true,
                            maxTextSize = LocalTextStyle.current.fontSize,
                            minTextSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val code = remember(episode.href) { episode.flags.firstOrNull()?.bestCountryCode }
                    if (code != null) {
                        CountryImage(
                            code = code,
                            description = episode.flags.firstOrNull()?.title,
                            iconSize = 24.dp
                        )
                    }
                    Text(
                        text = episode.info
                    )
                }
            }
        }
    }
}