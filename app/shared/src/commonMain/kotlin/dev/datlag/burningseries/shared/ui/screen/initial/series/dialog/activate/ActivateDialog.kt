package dev.datlag.burningseries.shared.ui.screen.initial.series.dialog.activate

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun ActivateDialog(component: ActivateComponent) {
    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.activate_episode_title),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = stringResource(SharedRes.strings.activate_episode_text))
        },
        confirmButton = {
            Button(
                onClick = {
                    component.activate()
                }
            ) {
                Text(text = stringResource(SharedRes.strings.activate))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(SharedRes.strings.later))
            }
        }
    )
}