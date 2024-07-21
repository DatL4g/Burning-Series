package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.lighten
import com.vanniktech.blurhash.BlurHash
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.common.decode
import dev.datlag.burningseries.common.toDuration
import dev.datlag.burningseries.database.CombinedEpisode
import dev.datlag.burningseries.model.Series
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.TopStartCornerShape
import dev.datlag.tooling.compose.onClick
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.shapes
import dev.datlag.tooling.compose.platform.typography
import kotlin.math.roundToInt

@Composable
internal fun EpisodeItem(
    item: CombinedEpisode,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClick: (CombinedEpisode) -> Unit,
    onMarkWatched: (CombinedEpisode) -> Unit,
    onMarkUnwatched: (CombinedEpisode) -> Unit,
    onActivate: (CombinedEpisode) -> Unit
) {
    val colors = CardDefaults.elevatedCardColors()

    ElevatedCard(
        modifier = modifier
            .clip(Platform.shapes().medium)
            .onClick(
                enabled = !isLoading && item.hasHoster,
                onClick = {
                    onClick(item)
                },
                onDoubleClick = {
                    if (item.isFinished) {
                        onMarkUnwatched(item)
                    } else {
                        onMarkWatched(item)
                    }
                },
                onLongClick = {
                    onActivate(item)
                }
            ),
        colors = CardColors(
            containerColor = if (item.hasHoster) colors.containerColor else colors.disabledContainerColor,
            contentColor = if (item.hasHoster) colors.contentColor else colors.disabledContentColor,
            disabledContainerColor = colors.disabledContainerColor,
            disabledContentColor = colors.disabledContentColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(84.dp)
                    .aspectRatio(1.75F, true)
                    .clip(Platform.shapes().medium),
                contentAlignment = Alignment.Center
            ) {
                val background = if (LocalDarkMode.current) {
                    LocalContentColor.current.darken(5F)
                } else {
                    LocalContentColor.current.lighten(5F)
                }
                Box(modifier = Modifier.fillMaxSize().background(background))

                val imageBitmap = remember(item.blurHash) {
                    BlurHash.decode(
                        hash = item.blurHash,
                        width = 175.dp.value.roundToInt(),
                        height = 100.dp.value.roundToInt()
                    )
                }

                imageBitmap?.let {
                    Image(
                        modifier = Modifier.matchParentSize(),
                        bitmap = it,
                        contentDescription = null
                    )
                }

                if (item.isFinished) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Platform.colorScheme().primary
                    )
                } else if (item.isWatching) {
                    Icon(
                        imageVector = Icons.Rounded.PauseCircle,
                        contentDescription = null,
                        tint = Platform.colorScheme().primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = null
                    )
                }
                if (item.number > 0) {
                    Text(
                        modifier = Modifier
                            .clip(
                                TopStartCornerShape(
                                    baseShape = Platform.shapes().medium,
                                    otherCorner = 0.dp
                                )
                            ).background(Color.White)
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        text = item.number.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator()
                }
            }
            Column(
                modifier = Modifier
                    .defaultMinSize(minHeight = 84.dp)
                    .fillMaxHeight()
                    .weight(1F),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.mainTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (item.hasSubtitle) 1 else 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
                item.subTitle?.let { sub ->
                    Text(
                        text = sub,
                        maxLines = 1,
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.weight(1F))
                if (item.length > 0) {
                    Text(
                        text = "${item.progress.toDuration()} - ${item.length.toDuration()}",
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        style = Platform.typography().labelSmall
                    )
                }
            }
        }
    }
}