package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.lighten
import dev.datlag.burningseries.LocalDarkMode
import dev.datlag.burningseries.model.Series

@Composable
internal fun EpisodeItem(
    item: Series.Episode,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        onClick = {

        },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .aspectRatio(1.75F, true)
                    .clip(MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                val background = if (LocalDarkMode.current) {
                    LocalContentColor.current.darken(5F)
                } else {
                    LocalContentColor.current.lighten(5F)
                }
                Box(modifier = Modifier.fillMaxSize().background(background))
                Icon(
                    imageVector = Icons.Rounded.PlayCircle,
                    contentDescription = null
                )
            }
            Text(text = item.title)
        }
    }
}