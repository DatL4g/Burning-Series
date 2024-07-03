package dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phonelink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.close
import dev.datlag.burningseries.composeapp.generated.resources.sync_settings
import dev.datlag.burningseries.composeapp.generated.resources.sync_settings_connecting
import dev.datlag.burningseries.composeapp.generated.resources.sync_settings_not_found
import dev.datlag.burningseries.composeapp.generated.resources.sync_settings_sending
import dev.datlag.burningseries.composeapp.generated.resources.sync_settings_time
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
fun SyncDialog(component: SyncComponent) {
    val sendingTo by component.sendingTo.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = { },
        icon = {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Phonelink,
                    contentDescription = null
                )
                if (!sendingTo.isNullOrBlank()) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp
                    )
                }
            }
        },
        title = {
            Text(text = stringResource(Res.string.sync_settings))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
            ) {
                val deviceNotFound by component.deviceNotFound.collectAsStateWithLifecycle()
                val takingTime by component.takingTime.collectAsStateWithLifecycle()

                if (deviceNotFound) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Res.string.sync_settings_not_found),
                        textAlign = TextAlign.Center
                    )
                } else {
                    val text = sendingTo?.ifBlank { null }?.let {
                        stringResource(Res.string.sync_settings_sending, it)
                    } ?: stringResource(Res.string.sync_settings_connecting)

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text,
                        textAlign = TextAlign.Center
                    )

                    if (takingTime) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.sync_settings_time),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(Res.string.close))
            }
        }
    )
}