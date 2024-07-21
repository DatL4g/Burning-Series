package dev.datlag.burningseries.ui.navigation.screen.medium.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.season_placeholder
import dev.datlag.burningseries.composeapp.generated.resources.select_language
import dev.datlag.burningseries.composeapp.generated.resources.select_season
import dev.datlag.burningseries.other.CountryImage
import dev.datlag.burningseries.ui.navigation.screen.medium.MediumComponent
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.PlatformButton
import dev.datlag.tooling.compose.platform.PlatformButtonScale
import dev.datlag.tooling.compose.platform.PlatformText
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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

        val seasonDialog = rememberUseCaseState()
        val languageDialog = rememberUseCaseState()

        OptionDialog(
            state = seasonDialog,
            selection = OptionSelection.Single(
                options = seasonList.map { season ->
                    Option(
                        titleText = season.title.toIntOrNull()?.let {
                            stringResource(Res.string.season_placeholder, it)
                        } ?: season.title,
                        disabled = selectedSeason == season
                    )
                },
                onSelectOption = { option, _ ->
                    component.season(seasonList.toList()[option])
                }
            ),
            config = OptionConfig(
                mode = if (seasonList.size > 5) {
                    DisplayMode.GRID_VERTICAL
                } else {
                    DisplayMode.LIST
                }
            ),
            header = Header.Default(
                icon = IconSource(
                    imageVector = Icons.AutoMirrored.Rounded.ViewList
                ),
                title = stringResource(Res.string.select_season)
            )
        )

        OptionDialog(
            state = languageDialog,
            selection = OptionSelection.Single(
                options = languageList.map { lang ->
                    Option(
                        titleText = lang.title,
                        disabled = selectedLanguage == lang,
                        customView = { selected ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val textColor = if (selected) {
                                    Platform.colorScheme().primary
                                } else {
                                    Platform.colorScheme().onSurface
                                }

                                Box(
                                    modifier = Modifier.padding(start = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CountryImage.showFlags(
                                        code = lang.value,
                                        showBorder = true,
                                        iconSize = 24.dp
                                    )
                                }
                                Text(
                                    modifier = Modifier
                                        .padding(
                                            top = 16.dp,
                                            bottom = 16.dp,
                                            start = 16.dp
                                        )
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    text = lang.title,
                                    maxLines = 1,
                                    style = Platform.typography().labelLarge,
                                    color = textColor
                                )
                            }
                        }
                    )
                },
                onSelectOption = { option, _ ->
                    component.language(languageList.toList()[option])
                }
            ),
            config = OptionConfig(
                mode = DisplayMode.LIST
            ),
            header = Header.Default(
                icon = IconSource(
                    imageVector = Icons.Rounded.Translate
                ),
                title = stringResource(Res.string.select_language)
            )
        )

        AnimatedVisibility(
            modifier = Modifier.weight(1F),
            visible = selectedSeason != null
        ) {
            selectedSeason?.let { season ->
                PlatformButton(
                    onClick = {
                        seasonDialog.show()
                    },
                    enabled = seasonList.size > 1,
                    scale = PlatformButtonScale.default(
                        focusedScale = 1.02f,
                    )
                ) {
                    PlatformText(
                        text = season.title.toIntOrNull()?.let {
                            stringResource(Res.string.season_placeholder, it)
                        } ?: season.title
                    )
                }
            }
        }
        AnimatedVisibility(
            modifier = Modifier.weight(1F),
            visible = selectedLanguage != null
        ) {
            selectedLanguage?.let { lang ->
                PlatformButton(
                    onClick = {
                        languageDialog.show()
                    },
                    enabled = languageList.size > 1,
                    scale = PlatformButtonScale.default(
                        focusedScale = 1.02f
                    )
                ) {
                    CountryImage.showFlags(
                        code = lang.value,
                        iconSize = ButtonDefaults.IconSize,
                        showBorder = true
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    PlatformText(text = lang.title)
                }
            }
        }
    }
}