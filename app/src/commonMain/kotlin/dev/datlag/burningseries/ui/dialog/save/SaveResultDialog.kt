package dev.datlag.burningseries.ui.dialog.save

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.onClick
import dev.datlag.burningseries.common.trimHref
import dev.datlag.burningseries.ui.custom.DialogSurface
import dev.datlag.burningseries.ui.custom.FlowAlertDialog

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SaveResultDialog(component: SaveResultComponent) {
    val strings = LocalStringRes.current

    DialogSurface(modifier = Modifier.onClick {
        component.onDismissClicked()
    }) {
        FlowAlertDialog(
            modifier = Modifier.defaultMinSize(minWidth = 400.dp, minHeight = 200.dp),
            onDismissRequest = {
                component.onDismissClicked()
            },
            title = {
                Text(
                    text = if (component.success) strings.saveSuccessHeader else strings.saveErrorHeader,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            },
            text = {
                Text(
                    text = if (component.success) strings.saveSuccess else strings.saveError,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            },
            neutralButton = { _, textLayout ->
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
                        contentDescription = LocalStringRes.current.watch,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = LocalStringRes.current.watch,
                        onTextLayout = textLayout
                    )
                }
            },
            dismissButton = { _, textLayout ->
                TextButton(
                    onClick = {
                        component.backClicked()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = LocalStringRes.current.back,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = LocalStringRes.current.back,
                        onTextLayout = textLayout
                    )
                }
            },
            confirmButton = { _, textLayout ->
                TextButton(
                    onClick = {
                        component.onDismissClicked()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = LocalStringRes.current.`continue`,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = LocalStringRes.current.`continue`,
                        onTextLayout = textLayout
                    )
                }
            }
        )
    }
}