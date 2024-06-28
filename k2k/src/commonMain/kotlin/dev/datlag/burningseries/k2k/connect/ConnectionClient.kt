package dev.datlag.burningseries.k2k.connect

import dev.datlag.burningseries.k2k.Host
import dev.datlag.tooling.async.suspendCatching
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.Dispatchers

internal data object ConnectionClient {
    suspend fun send(
        bytes: ByteArray,
        host: Host,
        port: Int
    ) {
        suspendCatching {
            val socketAddress = InetSocketAddress(host.hostAddress, port)
            val socket = aSocket(SelectorManager(Dispatchers.IO))
                .tcp()
                .connect(socketAddress) {
                    socketTimeout = 20000
                    reuseAddress = true
                }

            val writeChannel = socket.openWriteChannel(autoFlush = true)
            writeChannel.writeFully(bytes, 0, bytes.size)
        }
    }
}