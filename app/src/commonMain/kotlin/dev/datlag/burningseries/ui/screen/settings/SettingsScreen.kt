package dev.datlag.burningseries.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.OnWarning
import dev.datlag.burningseries.common.Warning
import dev.datlag.burningseries.common.fillWidthInPortraitMode
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.ui.custom.InfoCard
import dev.datlag.burningseries.ui.custom.dragdrop.DragDropColumn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: SettingsComponent) {
    val hosterList by component.hosterList.collectAsState(component.hosterList.getValueBlocking(emptyList()))

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        component.onGoBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = LocalStringRes.current.back,
                            tint = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                },
                title = {
                    Text(
                        text = LocalStringRes.current.settings
                    )
                },
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                elevation = 0.dp
            )
        }
    ) {
        DragDropColumn(
            items = hosterList,
            onSwap = { old, new ->
                component.swapHoster(old, new)
            },
            itemContent = {index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1F).padding(16.dp),
                            text = item.name
                        )
                        if (index > 0) {
                            IconButton(onClick = {
                                component.swapHoster(index, index - 1)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropUp,
                                    contentDescription = LocalStringRes.current.moveUp
                                )
                            }
                        }
                        if (index < hosterList.size - 1) {
                            IconButton(onClick = {
                                component.swapHoster(index, index + 1)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = LocalStringRes.current.moveDown
                                )
                            }
                        }
                    }
                }
            },
            itemsBefore = {
                item {
                    var errorCardMinWidth by remember { mutableStateOf(0) }

                    InfoCard(
                        title = LocalStringRes.current.hosterOrder,
                        text = LocalStringRes.current.hosterOrderText,
                        backgroundColor = Color.Warning,
                        contentColor = Color.OnWarning,
                        icon = Icons.Default.FormatListNumbered,
                        modifier = Modifier.fillWidthInPortraitMode().onSizeChanged {
                            errorCardMinWidth = it.width
                        }
                    )
                    if (hosterList.isEmpty()) {
                        InfoCard(
                            title = LocalStringRes.current.noHoster,
                            text = LocalStringRes.current.noHosterText,
                            backgroundColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            icon = Icons.Default.Report,
                            modifier = Modifier
                                .fillWidthInPortraitMode()
                                .defaultMinSize(minWidth = Dp(errorCardMinWidth.toFloat()))
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            },
            itemsAfter = {
                item {
                    Text(
                        text = LocalStringRes.current.copyright.format(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}