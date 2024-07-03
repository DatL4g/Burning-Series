package dev.datlag.burningseries.ui.navigation.screen.activate.dialog.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.activate_error_text
import dev.datlag.burningseries.composeapp.generated.resources.activate_success_text
import dev.datlag.burningseries.composeapp.generated.resources.back
import dev.datlag.burningseries.composeapp.generated.resources.close
import dev.datlag.burningseries.composeapp.generated.resources.error
import dev.datlag.burningseries.composeapp.generated.resources.success
import dev.datlag.burningseries.composeapp.generated.resources.warning
import dev.datlag.burningseries.composeapp.generated.resources.watch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorDialog(component: ErrorComponent) {
    val isWarning = remember { component.stream.isNotEmpty() }

    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        icon = {
            Icon(
                imageVector = if (isWarning) Icons.Rounded.Warning else Icons.Rounded.Error,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(if (isWarning) Res.string.warning else Res.string.error),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(Res.string.activate_error_text))
                component.stream.ifEmpty { null }?.let { stream ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            component.watch(stream)
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(Res.string.watch))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    component.back()
                }
            ) {
                Text(text = stringResource(Res.string.back))
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