package dev.datlag.burningseries.ui.screen.series

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.common.focusRequesterIf
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.other.Logger

@Composable
fun ColumnScope.SeriesLanguageSeasonButtons(
    component: SeriesComponent,
    languages: List<Series.Language>?,
    seasons: List<Series.Season>?,
    selectedLanguage: String?,
    selectedSeason: Series.Season?,
    seasonText: String?,
) = with(this) {
    val selectedLang = languages?.find { it.value.equals(selectedLanguage, true) }

    if (selectedLang != null) {
        Button(
            onClick = {
                component.showDialog(DialogConfig.Language(
                    languages,
                    selectedLang.value
                ))
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            enabled = languages.size > 1
        ) {
            Text(
                text = selectedLang.text,
                maxLines = 1
            )
        }
    }

    if (seasonText != null && seasons != null) {
        Button(
            onClick = {
                component.showDialog(DialogConfig.Season(
                    seasons,
                    selectedSeason
                ))
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            enabled = seasons.size > 1
        ) {
            Text(
                text = seasonText,
                maxLines = 1
            )
        }
    }
}