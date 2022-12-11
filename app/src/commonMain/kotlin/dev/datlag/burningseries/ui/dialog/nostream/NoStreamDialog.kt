package dev.datlag.burningseries.ui.dialog.nostream

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoStreamDialog(component: NoStreamComponent) {
    AlertDialog(
        modifier = Modifier.defaultMinSize(minWidth = 300.dp),
        onDismissRequest = {
            component.onDismissClicked()
        },
        title = {
            Text(
                text = "No streaming source",
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1
            )
        },
        text = {
            Text(
                text = "No streaming source found for this episode"
            )
        },
        confirmButton = {
            TextButton(onClick = {
                component.onConfirmActivate()
            }, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
                Text(
                    text = "Activate"
                )
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
                    contentDescription = null
                )
                Text(
                    text = "Close"
                )
            }
        }
    )
}
