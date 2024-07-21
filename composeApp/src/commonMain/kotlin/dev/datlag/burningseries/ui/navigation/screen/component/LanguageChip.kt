package dev.datlag.burningseries.ui.navigation.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.languageByCode
import dev.datlag.burningseries.model.Home
import dev.datlag.burningseries.other.CountryImage
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformClickableChipBorder
import dev.datlag.tooling.compose.platform.PlatformClickableChipColors
import dev.datlag.tooling.compose.platform.PlatformSuggestionChip
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.colorScheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageChip(
    flag: Home.Episode.Flag?,
    modifier: Modifier = Modifier,
    labelColor: Color = Platform.colorScheme().onPrimaryContainer,
    containerColor: Color = Platform.colorScheme().primaryContainer
) {
    if (flag != null) {
        PlatformSuggestionChip(
            onClick = { },
            modifier = modifier.wrapContentHeight().height(32.dp),
            colors = PlatformClickableChipColors.suggestion(
                disabledContainerColor = containerColor,
                disabledContentColor = labelColor
            ),
            enabled = false,
            icon = {
                flag.bestCountryCode?.let {
                    CountryImage.showFlags(
                        code = it,
                        iconSize = SuggestionChipDefaults.IconSize
                    )
                }
            },
            label = {
                PlatformText(
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