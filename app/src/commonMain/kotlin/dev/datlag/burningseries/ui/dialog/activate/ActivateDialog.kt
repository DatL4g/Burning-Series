package dev.datlag.burningseries.ui.dialog.activate

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.ui.custom.DialogSurface
import dev.datlag.burningseries.ui.custom.FlowAlertDialog
import dev.datlag.burningseries.other.Logger

@Composable
fun ActivateDialog(component: ActivateComponent) {
    val strings = LocalStringRes.current

    DialogSurface(modifier = Modifier.onClick {
        component.onDismissClicked()
    }) {
        FlowAlertDialog(
            modifier = Modifier.defaultMinSize(minWidth = 400.dp, minHeight = 250.dp),
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text(
                    text = component.episode.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                )
            },
            text = {
                Text(
                    text = strings.activateText,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                )
            },
            neutralButton = { _, textLayout ->
                TextButton(
                    onClick = {
                        strings.openInBrowser(Constants.getBurningSeriesUrl(component.episode.href))
                        component.onDismissClicked()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = strings.browser,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = strings.browser,
                        onTextLayout = textLayout
                    )
                }
            },
            dismissButton = { _, textLayout ->
                TextButton(
                    onClick = {
                        component.onDismissClicked()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.close,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = strings.close,
                        onTextLayout = textLayout
                    )
                }
            },
            confirmButton = { _, textLayout ->
                TextButton(
                    onClick = {
                        component.onConfirmActivate()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.activate,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = strings.activate,
                        onTextLayout = textLayout
                    )
                }
            }
        )
    }
}