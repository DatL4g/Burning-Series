package dev.datlag.burningseries.ui.navigation.screen.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.languageByCode
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.other.CountryImage
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageChip(
    flag: Home.Episode.Flag?,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    if (flag != null) {
        SuggestionChip(
            onClick = { },
            modifier = modifier.wrapContentHeight().height(32.dp),
            border = null,
            colors = SuggestionChipDefaults.suggestionChipColors(
                labelColor = labelColor,
                containerColor = containerColor
            ),
            icon = {
                flag.bestCountryCode?.let {
                    CountryImage.showFlags(
                        code = it,
                        iconSize = SuggestionChipDefaults.IconSize
                    )
                }
            },
            label = {
                Text(
                    text = flag.title ?: flag.languageByCode?.let { stringResource(it) } ?: "",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    softWrap = true
                )
            }
        )
    }
}