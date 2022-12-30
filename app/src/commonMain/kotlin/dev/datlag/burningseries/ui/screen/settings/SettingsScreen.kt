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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.LocalStringRes
import dev.datlag.burningseries.common.*
import dev.datlag.burningseries.other.Constants
import dev.datlag.burningseries.ui.custom.InfoCard
import dev.datlag.burningseries.ui.custom.dragdrop.DragDropColumn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(component: SettingsComponent) {
    val hosterList by component.hosterList.collectAsState(component.hosterList.getValueBlocking(emptyList()))
    val newRelease by component.newRelease.collectAsState(null)

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
                            text = (index + 1).toString(),
                            modifier = Modifier.padding(start = 16.dp),
                            fontWeight = FontWeight.Bold
                        )
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
                        } else {
                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }
                }
            },
            itemsBefore = {
                item {
                    val strings = LocalStringRes.current
                    var cardMinWidth by remember { mutableStateOf(0) }

                    if (newRelease != null) {
                        InfoCard(
                            title = newRelease!!.title,
                            text = strings.newRelease,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            icon = Icons.Default.NewReleases,
                            modifier = Modifier
                                .fillWidthInPortraitMode()
                                .defaultMinSize(minWidth = Dp(cardMinWidth.toFloat()))
                                .padding(vertical = 16.dp)
                                .onClick {
                                strings.openInBrowser(newRelease!!.htmlUrl.ifEmpty {
                                    Constants.GITHUB_REPOSITORY_URL
                                })
                            }.onSizeChanged {
                                if (it.width > cardMinWidth) {
                                    cardMinWidth = it.width
                                }
                            }
                        )
                    }


                    InfoCard(
                        title = LocalStringRes.current.hosterOrder,
                        text = LocalStringRes.current.hosterOrderText,
                        backgroundColor = Color.Warning,
                        contentColor = Color.OnWarning,
                        icon = Icons.Default.FormatListNumbered,
                        modifier = Modifier
                            .fillWidthInPortraitMode()
                            .defaultMinSize(minWidth = Dp(cardMinWidth.toFloat()))
                            .onSizeChanged {
                            if (it.width > cardMinWidth) {
                                cardMinWidth = it.width
                            }
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
                                .defaultMinSize(minWidth = Dp(cardMinWidth.toFloat()))
                                .padding(vertical = 16.dp)
                                .onSizeChanged {
                                    if (it.width > cardMinWidth) {
                                        cardMinWidth = it.width
                                    }
                                }
                        )
                    } else {
                        Text(
                            text = LocalStringRes.current.mostPreferred.format("1"),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        Text(
                            text = LocalStringRes.current.leastPreferred.format(max(hosterList.size, 2).toString()),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
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