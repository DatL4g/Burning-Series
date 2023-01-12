package dev.datlag.burningseries.ui.dialog.save

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.trimHref
import dev.datlag.burningseries.ui.custom.DialogSurface

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SaveResultDialog(component: SaveResultComponent) {
    val strings = LocalStringRes.current

    DialogSurface {
        AlertDialog(
            modifier = Modifier.defaultMinSize(minWidth = 400.dp),
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text(
                    text = if (component.success) strings.saveSuccessHeader else strings.saveErrorHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2
                )
            },
            text = {
                Text(
                    text = if (component.success) strings.saveSuccess else strings.saveError
                )
            },
            buttons = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            component.watchClicked(component.stream!!)
                        },
                        enabled = component.stream != null && (component.series.episodes.any {
                            it.href.trimHref().equals(component.scrapedEpisodeHref.trimHref(), true) || it.hoster.any { hoster ->
                                hoster.href.trimHref().equals(component.scrapedEpisodeHref.trimHref(), true)
                            }
                        } || (component.episode.href.trimHref().equals(component.scrapedEpisodeHref.trimHref(), true) || component.episode.hoster.any { hoster ->
                            hoster.href.trimHref().equals(component.scrapedEpisodeHref.trimHref(), true)
                        }))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = LocalStringRes.current.watch
                        )
                        Text(
                            text = LocalStringRes.current.watch
                        )
                    }
                    Spacer(
                        modifier = Modifier.weight(1F)
                    )
                    TextButton(
                        onClick = {
                            component.backClicked()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = LocalStringRes.current.back
                        )
                        Text(
                            text = LocalStringRes.current.back
                        )
                    }
                    TextButton(
                        onClick = {
                            component.onDismissClicked()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = LocalStringRes.current.`continue`
                        )
                        Text(
                            text = LocalStringRes.current.`continue`
                        )
                    }
                }
            }
        )
    }
}