package dev.datlag.burningseries.ui.dialog.language

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LanguageDialog(component: LanguageComponent) {

    if (component.languages.isNotEmpty() && !component.selectedLanguage.isNullOrEmpty()) {
        val currentSelectedLanguage = remember { component.languages.find { it.value.equals(component.selectedLanguage, true) } }
        var selectedItem by remember { mutableStateOf(currentSelectedLanguage) }

        DialogSurface(
            modifier = Modifier.onClick {
                component.onDismissClicked()
            }
        ) {
            AlertDialog(
                onDismissRequest = {
                    component.onDismissClicked()
                },
                title = {
                    Text(
                        text = LocalStringRes.current.selectLanguage,
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
                        component.languages.forEach {
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
                                    text = it.text,
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
                            if (selectedItem != null && selectedItem != currentSelectedLanguage) {
                                component.onConfirmNewLanguage(selectedItem!!)
                            } else {
                                component.onDismissClicked()
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
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