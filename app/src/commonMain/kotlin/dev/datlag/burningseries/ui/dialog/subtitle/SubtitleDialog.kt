package dev.datlag.burningseries.ui.dialog.subtitle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.ui.custom.DialogSurface

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubtitleDialog(component: SubtitleComponent) {
    if (component.subtitles.isNotEmpty()) {
        var selectedItem by remember { mutableStateOf(component.selectedLanguage) }

        DialogSurface(
            modifier = Modifier.onClick {
                component.onDismissClicked()
            }
        ) {
            AlertDialog(
                modifier = Modifier.defaultMinSize(minWidth = 400.dp),
                onDismissRequest = {
                    component.onDismissClicked()
                },
                title = {
                    Text(
                        text = LocalStringRes.current.selectSubtitle,
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
                        horizontalAlignment = Alignment.Start
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
                            Text(
                                text = LocalStringRes.current.none,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = true
                            )
                        }
                        component.subtitles.forEach {
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
                    TextButton(
                        onClick = {
                            component.onConfirmSubtitle(selectedItem)
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = LocalStringRes.current.confirm)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            component.onDismissClicked()
                        },
                        modifier = Modifier.padding(bottom = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = LocalStringRes.current.close)
                    }
                }
            )
        }
    }
}