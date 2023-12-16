package dev.datlag.burningseries.shared.ui.screen.initial.home.dialog.sekret

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.shared.SharedRes
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SekretDialog(component: SekretComponent) {
    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.sekret_unavailable_title),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = stringResource(SharedRes.strings.sekret_unavailable_text))
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