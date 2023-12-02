package dev.datlag.burningseries.shared.ui.screen.initial.series.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.ui.custom.CountryImage
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeasonAndLanguageButtons(
    selectedSeason: Series.Season?,
    selectedLanguage: Series.Language?,
    seasons: List<Series.Season>,
    languages: List<Series.Language>,
    onSeasonClick: (Series.Season?) -> Unit,
    onLanguageClick: (Series.Language?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedSeason != null) {
            Button(
                onClick = {
                    onSeasonClick(selectedSeason)
                },
                enabled = seasons.size > 1,
                modifier = Modifier.weight(1F)
            ) {
                val seasonText = if (selectedSeason.title.toIntOrNull() != null) {
                    stringResource(SharedRes.strings.season_placeholder, selectedSeason.title)
                } else {
                    selectedSeason.title
                }
                Text(text = seasonText)
            }
        }
        if (selectedLanguage != null) {
            Button(
                onClick = {
                    onLanguageClick(selectedLanguage)
                },
                enabled = languages.size > 1,
                modifier = Modifier.weight(1F)
            ) {
                CountryImage(
                    code = selectedLanguage.value,
                    description = selectedLanguage.title,
                    iconSize = ButtonDefaults.IconSize
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = selectedLanguage.title)
            }
        }
    }
}