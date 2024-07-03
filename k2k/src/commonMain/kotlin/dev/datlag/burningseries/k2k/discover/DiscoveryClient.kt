package dev.datlag.burningseries.k2k.discover

import dev.datlag.burningseries.k2k.Constants
import dev.datlag.tooling.async.suspendCatching
import dev.datlag.tooling.scopeCatching
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import dev.datlag.burningseries.k2k.NetInterface

internal data object DiscoveryClient {
    private var socket = aSocket(SelectorManager(Dispatchers.IO)).udp()

    private var broadcastJob: Job? = null

    internal fun startBroadcasting(
        port: Int,
        ping: Long,
        data: ByteArray,
        scope: CoroutineScope
    ) {
        broadcastJob?.cancel()
        broadcastJob = scope.launch(Dispatchers.IO) {
            while (currentCoroutineContext().isActive) {
                send(port, data)
                delay(ping)
            }
        }
    }

    internal fun stopBroadcasting() {
        broadcastJob?.cancel()
        socket = aSocket(SelectorManager(Dispatchers.IO)).udp()
    }

    private suspend fun send(port: Int, data: ByteArray) {
        suspend fun writeToSocket(address: String, port: Int) = suspendCatching {
            val socketConnection = socket.connect(InetSocketAddress(address, port)) {
                broadcast = true
                reuseAddress = true
            }

            val output = socketConnection.openWriteChannel(autoFlush = true)
            output.writeFully(data, 0, data.size)
            output.close()
            socketConnection.close()
        }

        writeToSocket(Constants.BROADCAST_ADDRESS, port)
        for (address in NetInterface.getAddresses()) {
            writeToSocket(address, port)
        }
    }
}