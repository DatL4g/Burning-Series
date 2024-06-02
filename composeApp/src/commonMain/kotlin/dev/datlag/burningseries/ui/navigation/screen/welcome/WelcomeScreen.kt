package dev.datlag.burningseries.ui.navigation.screen.welcome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoubleArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import dev.datlag.burningseries.composeapp.generated.resources.AniFlow
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.app_name
import dev.datlag.burningseries.composeapp.generated.resources.lets_go
import dev.datlag.burningseries.composeapp.generated.resources.welcome_to
import dev.datlag.burningseries.other.Project
import dev.datlag.burningseries.settings.model.Language
import dev.datlag.burningseries.ui.custom.AndroidFixWindowSize
import dev.datlag.burningseries.ui.navigation.screen.welcome.component.CompactScreen
import dev.datlag.burningseries.ui.navigation.screen.welcome.component.LanguageSelection
import dev.datlag.burningseries.ui.navigation.screen.welcome.component.WideScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(component: WelcomeComponent) {
    AndroidFixWindowSize {
        var selectedLanguage by remember { mutableStateOf<Language?>(null) }

        when (calculateWindowSizeClass().widthSizeClass) {
            WindowWidthSizeClass.Compact -> CompactScreen {
                content(
                    selectedLanguage = selectedLanguage,
                    onSelect = {
                        selectedLanguage = it
                    },
                    onStart = {
                        selectedLanguage?.let {
                            component.start(it)
                        }
                    }
                )
            }
            else -> WideScreen {
                content(
                    selectedLanguage = selectedLanguage,
                    onSelect = {
                        selectedLanguage = it
                    },
                    onStart = {
                        selectedLanguage?.let {
                            component.start(it)
                        }
                    }
                )
            }
        }
    }
}

private fun LazyListScope.content(
    selectedLanguage: Language?,
    onSelect: (Language) -> Unit,
    onStart: () -> Unit
) {
    item {
        Text(
            modifier = Modifier.fillParentMaxWidth(0.7F),
            text = buildAnnotatedString {
                append(stringResource(Res.string.welcome_to))
                append(' ')
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(stringResource(Res.string.app_name))
                }
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
    }
    item {
        LanguageSelection(
            modifier = Modifier.fillParentMaxWidth(0.7F),
            selected = selectedLanguage,
            onSelect = onSelect
        )
    }
    if (selectedLanguage == Language.JapaneseSubtitle) {
        item {
            val uriHandler = LocalUriHandler.current

            OutlinedButton(
                modifier = Modifier.fillParentMaxWidth(0.7F),
                onClick = {
                    uriHandler.openUri(Project.AniFlow.googlePlay)
                }
            ) {
                Image(
                    modifier = Modifier.size(ButtonDefaults.IconSize).clip(CircleShape),
                    painter = painterResource(Project.AniFlow.image),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(Project.AniFlow.title))
            }
        }
    }
    item {
        Button(
            onClick = onStart,
            modifier = Modifier.fillParentMaxWidth(0.7F),
            enabled = selectedLanguage != null
        ) {
            Text(text = stringResource(Res.string.lets_go))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Icon(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                imageVector = Icons.Rounded.DoubleArrow,
                contentDescription = null
            )
        }
    }
}
