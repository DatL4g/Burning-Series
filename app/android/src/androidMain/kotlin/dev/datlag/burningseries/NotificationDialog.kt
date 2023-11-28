package dev.datlag.burningseries

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.icerock.moko.resources.compose.stringResource
import dev.datlag.burningseries.shared.SharedRes

@Composable
fun NotificationDialog(text: String, onConfirm: () -> Unit, onDismiss: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = {
            onDismiss(false)
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = stringResource(SharedRes.strings.notifications)
            )
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.notifications),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = text)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss(false)
                }
            ) {
                Text(text = stringResource(SharedRes.strings.grant))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss(true)
                }
            ) {
                Text(text = stringResource(SharedRes.strings.deny))
            }
        }
    )
}