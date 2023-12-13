package dev.datlag.burningseries.shared.ui.screen.initial.series.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanniktech.blurhash.BlurHash
import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.*
import dev.datlag.burningseries.shared.ui.theme.TopLeftBottomRightRoundedShape
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun EpisodeItem(
    content: Series.Episode,
    dbEpisode: Episode?,
    isLoading: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val blurHash = remember(content.href) { BlurHash.random() }
    val enabled = content.hosters.isNotEmpty()

    val length = remember(dbEpisode) {
        max(dbEpisode?.length ?: 0L, 0L)
    }
    val progress = remember(dbEpisode) {
        max(dbEpisode?.progress ?: 0L, 0L)
    }
    val isFinished = remember(length, progress) {
        if (length > 0L && progress > 0L) {
            (progress.toDouble() / length.toDouble() * 100.0).toFloat() >= 85F
        } else {
            false
        }
    }

    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .focusScale(1.02F)
            .height(100.dp)
            .clip(MaterialTheme.shapes.medium)
            .onClick(
                enabled = enabled,
                onLongClick = onLongClick,
                onClick = onClick
            ).ifTrue(enabled) { bounceClick(0.95F) }.ifFalse(enabled) { alpha(0.5F) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1.75F, true)
                .clip(MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            val imageBitmap by remember(blurHash) {
                derivedStateOf {
                    BlurHash.decode(hash = blurHash, width = 175.dp.value.roundToInt(), height = 100.dp.value.roundToInt())
                }
            }
            imageBitmap?.let {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = it,
                    contentDescription = content.title
                )
            } ?: Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer))
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5F)))
            if (isFinished) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = content.title,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = content.title,
                    tint = Color.White
                )
            }
            Text(
                modifier = Modifier.clip(TopLeftBottomRightRoundedShape(
                    baseShape = MaterialTheme.shapes.medium,
                    otherSideRounding = 0.dp
                )).background(Color.White).align(Alignment.BottomEnd).padding(4.dp),
                text = content.episodeNumber,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (isLoading) {
                CircularProgressIndicator()
            }
        }
        Column(
            modifier = Modifier.fillMaxHeight().weight(1F)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1F),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = content.episodeTitle,
                    maxLines = 3
                )
            }
            if (length != 0L && progress != 0L) {
                Text(
                    text = stringResource(SharedRes.strings.episode_progress, progress.toDuration(), length.toDuration()),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}