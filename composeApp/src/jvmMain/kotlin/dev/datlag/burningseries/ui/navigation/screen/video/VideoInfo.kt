package dev.datlag.burningseries.ui.navigation.screen.video

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import dev.datlag.burningseries.common.icon
import dev.datlag.burningseries.common.isConnectedOrConnecting
import dev.datlag.kast.Kast
import dev.datlag.kast.UnselectReason
import dev.datlag.tooling.decompose.lifecycle.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VideoInfo(component: VideoComponent) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    component.back()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = null
                )
            }
        },
        title = {
            Text(text = component.episode.title)
        },
        actions = {
            val allAvailableDevices by Kast.allAvailableDevices.collectAsStateWithLifecycle()
            val connectionState by Kast.connectionState.collectAsStateWithLifecycle()

            if (allAvailableDevices.isNotEmpty()) {
                IconButton(
                    onClick = { },
                    enabled = Kast.isSupported
                ) {
                    Icon(
                        imageVector = connectionState.icon,
                        contentDescription = null
                    )
                }
            } else if (connectionState.isConnectedOrConnecting) {
                IconButton(
                    onClick = {
                        Kast.unselect(UnselectReason.disconnected)
                    }
                ) {
                    Icon(
                        imageVector = connectionState.icon,
                        contentDescription = null
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black,
            scrolledContainerColor = Color.Black,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}