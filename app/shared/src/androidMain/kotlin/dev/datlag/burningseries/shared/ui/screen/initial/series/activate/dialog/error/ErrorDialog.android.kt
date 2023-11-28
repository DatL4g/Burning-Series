package dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.error

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.ui.screen.initial.series.activate.dialog.error.ErrorComponent
import dev.icerock.moko.resources.compose.stringResource

@Composable
actual fun ErrorDialog(component: ErrorComponent) {
    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.activate_error_title),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = stringResource(SharedRes.strings.activate_error_text))
        },
        confirmButton = {
            Button(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(SharedRes.strings.close))
            }
        }
    )
}