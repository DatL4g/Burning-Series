package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.season_placeholder
import dev.datlag.burningseries.other.CountryImage
import dev.datlag.burningseries.ui.navigation.screen.medium.MediumComponent
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SeasonLanguageSection(
    component: MediumComponent,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        val selectedSeason by component.seriesSeason.collectAsStateWithLifecycle(null)
        val seasonList by component.seriesSeasonList.collectAsStateWithLifecycle(persistentListOf())
        val selectedLanguage by component.seriesLanguage.collectAsStateWithLifecycle(null)
        val languageList by component.seriesLanguageList.collectAsStateWithLifecycle(persistentListOf())


        AnimatedVisibility(
            modifier = Modifier.weight(1F),
            visible = selectedSeason != null
        ) {
            Button(
                onClick = {

                },
                enabled = seasonList.size > 1
            ) {
                Text(
                    text = selectedSeason?.title?.toIntOrNull()?.let {
                        stringResource(Res.string.season_placeholder, it)
                    } ?: selectedSeason!!.title
                )
            }
        }
        AnimatedVisibility(
            modifier = Modifier.weight(1F),
            visible = selectedLanguage != null
        ) {
            Button(
                onClick = {

                },
                enabled = languageList.size > 1
            ) {
                CountryImage.showFlags(
                    code = selectedLanguage?.value,
                    iconSize = ButtonDefaults.IconSize,
                    showBorder = true
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = selectedLanguage!!.title)
            }
        }
    }
}