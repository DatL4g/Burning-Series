package dev.datlag.burningseries.shared.ui.screen.video.dialog.subtitle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.ui.custom.CountryImage
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SubtitleDialog(component: SubtitleComponent) {
    var selectedItem by remember { mutableStateOf(component.initialChosen) }

    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.select_subtitle),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.selectable(
                        selected = selectedItem == null,
                        role = Role.RadioButton,
                        onClick = { selectedItem = null }
                    ).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedItem == null,
                        onClick = null
                    )
                    Icon(
                        imageVector = Icons.Default.SubtitlesOff,
                        contentDescription = stringResource(SharedRes.strings.subtitles_off),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(SharedRes.strings.subtitles_off),
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
                component.list.forEach {
                    val selected = selectedItem == it
                    Row(
                        modifier = Modifier.selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { selectedItem = it }
                        ).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null
                        )
                        CountryImage(
                            code = it.code,
                            description = it.title,
                            iconSize = 24.dp
                        )
                        Text(
                            text = it.title,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (component.initialChosen != selectedItem) {
                        component.choose(selectedItem)
                    } else {
                        component.dismiss()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(SharedRes.strings.confirm),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(SharedRes.strings.confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    component.dismiss()
                },
                modifier = Modifier.padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(SharedRes.strings.close),
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(SharedRes.strings.close))
            }
        }
    )
}