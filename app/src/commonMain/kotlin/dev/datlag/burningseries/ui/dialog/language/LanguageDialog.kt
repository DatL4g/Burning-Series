package dev.datlag.burningseries.ui.dialog.language

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun LanguageDialog(component: LanguageComponent) {
    val languages by component.languages.collectAsState(null)
    val selectedLanguage by component.selectedLanguage.collectAsState(null)

    if (!languages.isNullOrEmpty() && !selectedLanguage.isNullOrEmpty()) {
        var selectedItem by remember { mutableStateOf(languages!!.find { it.value.equals(selectedLanguage, true) }) }

        AlertDialog(
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text(
                    text = "Select language",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    languages!!.forEach {
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
                                text = it.text
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    component.onDismissClicked()
                }, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    component.onDismissClicked()
                }, modifier = Modifier.padding(bottom = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null
                    )
                    Text(text = "Close")
                }
            }
        )
    }
}