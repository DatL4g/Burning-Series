package dev.datlag.burningseries.ui.navigation.screen.home.dialog.sync

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phonelink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.close
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
            Text(text = "Sync")
        },
        text = {
            Text(text = "Discover and connect")
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