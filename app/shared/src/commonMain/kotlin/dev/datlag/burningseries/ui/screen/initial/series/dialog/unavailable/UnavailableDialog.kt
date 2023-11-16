package dev.datlag.burningseries.ui.screen.initial.series.dialog.unavailable

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UnavailableDialog(component: UnavailableComponent) {
    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.stream_unavailable_title),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = stringResource(SharedRes.strings.stream_unavailable_text))
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