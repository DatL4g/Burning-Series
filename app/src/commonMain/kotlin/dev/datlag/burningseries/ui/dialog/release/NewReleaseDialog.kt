package dev.datlag.burningseries.ui.dialog.release

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.ui.custom.DialogSurface

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewReleaseDialog(component: NewReleaseComponent) {
    val strings = LocalStringRes.current

    DialogSurface {
        AlertDialog(
            modifier = Modifier.defaultMinSize(minWidth = 400.dp),
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text(
                    text = component.newRelease.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2
                )
            },
            text = {
                Text(
                    text = strings.newRelease
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        strings.openInBrowser(component.newRelease.htmlUrl.ifEmpty {
                            Constants.GITHUB_REPOSITORY_URL
                        })
                        component.onDismissClicked()
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NewReleases,
                        contentDescription = component.newRelease.title
                    )
                    Text(
                        text = strings.view
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        component.onDismissClicked()
                    },
                    modifier = Modifier.padding(bottom = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = LocalStringRes.current.close
                    )
                    Text(
                        text = LocalStringRes.current.close
                    )
                }
            }
        )
    }
}