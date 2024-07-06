package dev.datlag.burningseries.k2k

import kotlinx.serialization.json.Json

internal data object Constants {
    val json: Json = Json {
        isLenient = true
    }

    const val BROADCAST_ADDRESS = "255.255.255.255"
    const val BROADCAST_SOCKET = "0.0.0.0"
}