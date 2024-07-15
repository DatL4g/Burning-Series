package dev.datlag.burningseries.ui.navigation.screen.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.SearchItem
import dev.datlag.burningseries.ui.navigation.screen.home.HomeComponent
import dev.datlag.tooling.compose.onClick
import kotlinx.collections.immutable.ImmutableCollection

@Composable
internal fun SearchResult(
    item: SearchItem,
    filterGenres: ImmutableCollection<String>,
    modifier: Modifier = Modifier,
    onClick: (SearchItem) -> Unit
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .onClick {
                onClick(item)
            }.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = item.mainTitle,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = if (item.hasSubtitle) 1 else 2
            )
            item.subTitle?.let { sub ->
                Text(
                    text = sub,
                    maxLines = 1
                )
            }
        }
        item.genre?.let { genre ->
            val info = if (filterGenres.contains(item.genre ?: "")) {
                HomeComponent.GenreFilterInfo(
                    containerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onPrimary,
                    border = null
                )
            } else if (item.isAnime) {
                HomeComponent.GenreFilterInfo(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    labelColor = MaterialTheme.colorScheme.onSecondary,
                    border = null
                )
            } else {
                HomeComponent.GenreFilterInfo(
                    containerColor = Color.Unspecified,
                    labelColor = Color.Unspecified,
                    border = SuggestionChipDefaults.suggestionChipBorder(true)
                )
            }

            SuggestionChip(
                onClick = { },
                label = { Text(text = genre) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = info.containerColor,
                    labelColor = info.labelColor                                         ),
                border = info.border
            )
        }
    }
}