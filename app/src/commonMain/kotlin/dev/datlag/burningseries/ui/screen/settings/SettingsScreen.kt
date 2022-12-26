package dev.datlag.burningseries.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.getValueBlocking
import dev.datlag.burningseries.ui.custom.dragdrop.DragDropColumn

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
                                    contentDescription = null
                                )
                            }
                        }
                        if (index < hosterList.size - 1) {
                            IconButton(onClick = {
                                component.swapHoster(index, index + 1)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            },
            itemsBefore = {
                item {
                    Text(text = "Hoster Order")
                }
            },
            itemsAfter = {
                item {
                    Text(text = "After Text")
                }
            }
        )
    }
}