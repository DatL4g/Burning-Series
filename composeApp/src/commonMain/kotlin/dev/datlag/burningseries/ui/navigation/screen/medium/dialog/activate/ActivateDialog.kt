package dev.datlag.burningseries.ui.navigation.screen.medium.dialog.activate

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.datlag.burningseries.composeapp.generated.resources.Res
import dev.datlag.burningseries.composeapp.generated.resources.activate
import dev.datlag.burningseries.composeapp.generated.resources.activate_episode_text
import dev.datlag.burningseries.composeapp.generated.resources.activate_episode_title
import dev.datlag.burningseries.composeapp.generated.resources.later
import org.jetbrains.compose.resources.stringResource

@Composable
fun ActivateDialog(component: ActivateComponent) {
    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Web,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.activate_episode_title),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Text(text = stringResource(Res.string.activate_episode_text))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    component.activate()
                }
            ) {
                Text(text = stringResource(Res.string.activate))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(Res.string.later))
            }
        }
    )
}