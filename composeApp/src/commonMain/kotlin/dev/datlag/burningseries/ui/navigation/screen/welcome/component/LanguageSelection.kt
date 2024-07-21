package dev.datlag.burningseries.ui.navigation.screen.welcome.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.datlag.burningseries.common.flags
import dev.datlag.burningseries.common.title
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.select_default_language_text
import dev.datlag.burningseries.other.CountryImage
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.tooling.Platform
import dev.datlag.tooling.compose.platform.typography
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelection(
    selected: Language?,
    modifier: Modifier = Modifier,
    onSelect: (Language) -> Unit
) {
    var exposed by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = exposed,
        onExpandedChange = { exposed = it }
    ) {
        TextButton(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            onClick = { exposed = true }
        ) {
            CountryImage.showFlags(
                collection = selected.flags,
                description = stringResource(selected.title),
                iconSize = ButtonDefaults.IconSize,
                shape = CircleShape
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(selected.title))
        }

        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = false),
            expanded = exposed,
            onDismissRequest = { exposed = false }
        ) {
            DropdownMenuItem(
                enabled = false,
                onClick = { },
                text = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.select_default_language_text),
                        style = Platform.typography().labelSmall
                    )
                },
                leadingIcon = {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null
                    )
                }
            )
            Language.all.forEach { lang ->
                DropdownMenuItem(
                    onClick = {
                        onSelect(lang)
                        exposed = false
                    },
                    text = {
                        Text(text = stringResource(lang.title))
                    },
                    leadingIcon = {
                        CountryImage.showFlags(
                            collection = lang.flags,
                            description = stringResource(lang.title),
                            iconSize = ButtonDefaults.IconSize,
                            showBorder = true,
                            shape = CircleShape
                        )
                    },
                    enabled = selected != lang
                )
            }
        }
    }
}