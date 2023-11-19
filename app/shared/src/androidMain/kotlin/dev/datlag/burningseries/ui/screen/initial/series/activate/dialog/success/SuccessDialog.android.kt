package dev.datlag.burningseries.ui.screen.initial.series.activate.dialog.success

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
actual fun SuccessDialog(component: SuccessComponent) {
    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.activate_success_title),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = stringResource(SharedRes.strings.activate_success_text))
        },
        confirmButton = {
            Button(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(SharedRes.strings.close))
            }
        },
        dismissButton = if (component.stream != null) {
            {
                Button(
                    onClick = {

                    }
                ) {
                    Text(text = stringResource(SharedRes.strings.watch))
                }
            }
        } else {
            null
        }
    )
}