package dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync

import androidx.compose.foundation.layout.Arrangement
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
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
fun SyncDialog(component: SyncComponent) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Phonelink,
                contentDescription = null
            )
        },
        title = {
            Text(text = "Sync Settings")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val deviceNotFound by component.deviceNotFound.collectAsStateWithLifecycle()
                val sending by component.sending.collectAsStateWithLifecycle()

                if (deviceNotFound) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Device not found, make sure you are in the same network",
                        textAlign = TextAlign.Center
                    )
                } else {
                    if (sending) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Syncing to your other device, please wait",
                            textAlign = TextAlign.Center
                        )
                        CircularProgressIndicator()
                    } else {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Connecting to your other device, please wait",
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