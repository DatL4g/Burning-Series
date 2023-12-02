package dev.datlag.burningseries.shared.ui.screen.video.dialog.cast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.datlag.burningseries.shared.SharedRes
import dev.datlag.burningseries.shared.common.lifecycle.collectAsStateWithLifecycle
import dev.datlag.burningseries.shared.common.onClick
import dev.datlag.burningseries.shared.ui.*
import dev.datlag.kast.*
import dev.icerock.moko.resources.compose.stringResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

@Composable
actual fun CastDialog(component: CastComponent) {
    val selectedItem by Kast.selectedDevice.collectAsStateWithLifecycle()
    val otherItems by Kast.allAvailableDevices.map { list -> list.mapNotNull {
        if (it.selected) {
            null
        } else {
            it
        }
    } }.collectAsStateWithLifecycle(initialValue = Kast.allAvailableDevices.value.mapNotNull {
        if (it.selected) {
            null
        } else {
            it
        }
    })
    var closeRequest by remember { mutableStateOf(false) }

    SideEffect {
        Kast.Router.activeDiscovery()
    }

    DisposableEffect(Unit) {
        onDispose {
            Kast.Router.passiveDiscovery()
        }
    }

    AlertDialog(
        onDismissRequest = {
            component.dismiss()
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(SharedRes.strings.casting),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedItem != null) {
                    Text(
                        text = stringResource(SharedRes.strings.selected),
                        fontWeight = FontWeight.Bold
                    )
                    DeviceInfo(
                        device = selectedItem!!,
                        onClick = {
                            Kast.unselect(UnselectReason.disconnected)
                            component.dismiss()
                        },
                        onConnected = {
                            if (closeRequest) {
                                component.dismiss()
                            }
                        }
                    )
                }
                if (otherItems.isEmpty() && selectedItem == null) {
                    Text(
                        text = stringResource(SharedRes.strings.available),
                        fontWeight = FontWeight.Bold
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (selectedItem == null) {
                    Text(
                        text = stringResource(SharedRes.strings.available),
                        fontWeight = FontWeight.Bold
                    )

                    otherItems.forEach {
                        DeviceInfo(
                            device = it,
                            onClick = {
                                closeRequest = true
                                Kast.select(it)
                            },
                            onConnected = {
                                if (closeRequest) {
                                    component.dismiss()
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    component.dismiss()
                }
            ) {
                Text(text = stringResource(SharedRes.strings.close))
            }
        },
        dismissButton = {
            if (selectedItem != null) {
                Button(
                    onClick = {
                        Kast.unselect(UnselectReason.disconnected)
                        component.dismiss()
                    }
                ) {
                    Text(text = stringResource(SharedRes.strings.disconnect))
                }
            }
        }
    )
}

@Composable
private fun DeviceInfo(device: Device, onClick: () -> Unit, onConnected: () -> Unit) {
    val icon = when (device.type) {
        is DeviceType.TV -> Icons.Default.Tv
        is DeviceType.SPEAKER -> Icons.Default.Speaker
        else -> Icons.Default.SmartDisplay
    }
    val isConnecting = when (device.connectionState) {
        is ConnectionState.CONNECTING -> true
        is ConnectionState.CONNECTED -> {
            onConnected()
            false
        }
        else -> false
    }

    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).clip(MaterialTheme.shapes.small).onClick { onClick() },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = device.name
            )
            if (isConnecting) {
                CircularProgressIndicator()
            }
        }
        Text(
            text = device.name,
            overflow = TextOverflow.Ellipsis,
            softWrap = true,
            maxLines = 1
        )
    }
}