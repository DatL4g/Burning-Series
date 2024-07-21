package dev.datlag.burningseries.ui.navigation.screen.home.dialog.settings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import dev.datlag.burningseries.common.flags
import dev.datlag.burningseries.common.title
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.select_default_language_text
import dev.datlag.burningseries.composeapp.generated.resources.select_language
import dev.datlag.burningseries.other.CountryImage
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.colorScheme
import dev.datlag.tooling.compose.platform.typography
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSection(
    languageFlow: Flow<Language?>,
    modifier: Modifier = Modifier,
    onSelect: (Language) -> Unit
) {
    val selectedLanguage by languageFlow.collectAsStateWithLifecycle(null)
    val useCase = rememberUseCaseState()
    val languages = remember { Language.all.toImmutableList() }

    OptionDialog(
        state = useCase,
        selection = OptionSelection.Single(
            options = languages.map { lang ->
                Option(
                    selected = lang == selectedLanguage,
                    titleText = stringResource(lang.title),
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
                                    code = lang.code,
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
                                text = stringResource(lang.title),
                                maxLines = 1,
                                style = Platform.typography().labelLarge,
                                color = textColor
                            )
                        }
                    }
                )
            },
            onSelectOption = { option, _ ->
                onSelect(languages[option])
            }
        ),
        config = OptionConfig(
            mode = DisplayMode.LIST
        ),
        header = Header.Default(
            icon = IconSource(imageVector = Icons.Rounded.Translate),
            title = stringResource(Res.string.select_language)
        )
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Translate,
            contentDescription = null
        )
        Text(
            text = stringResource(selectedLanguage?.title ?: Res.string.select_language)
        )
        Spacer(modifier = Modifier.weight(1F))
        IconButton(
            onClick = { useCase.show() }
        ) {
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null
            )
        }
    }
}