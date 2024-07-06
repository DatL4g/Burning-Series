package dev.datlag.burningseries.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.ui.graphics.vector.ImageVector
import dev.datlag.kast.ConnectionState

val ConnectionState.isConnectedOrConnecting: Boolean
    get() = when (this) {
        is ConnectionState.CONNECTED, is ConnectionState.CONNECTING -> true
        else -> false
    }

val ConnectionState.icon: ImageVector
    get() = if (isConnectedOrConnecting) {
        Icons.Rounded.CastConnected
    } else {
        Icons.Rounded.Cast
    }