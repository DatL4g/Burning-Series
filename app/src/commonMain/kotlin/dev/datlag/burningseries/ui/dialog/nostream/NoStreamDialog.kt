package dev.datlag.burningseries.ui.dialog.nostream

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.ui.custom.DialogSurface

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoStreamDialog(component: NoStreamComponent) {
    DialogSurface {
        AlertDialog(
            modifier = Modifier.defaultMinSize(minWidth = 400.dp),
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text(
                    text = LocalStringRes.current.noStreamingSourceHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            },
            text = {
                Text(
                    text = LocalStringRes.current.noStreamingSourceText,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        component.onConfirmActivate()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = LocalStringRes.current.activate
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
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = LocalStringRes.current.close
                    )
                }
            }
        )
    }
}
