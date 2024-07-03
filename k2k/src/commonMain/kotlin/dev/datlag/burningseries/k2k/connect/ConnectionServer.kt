package dev.datlag.burningseries.k2k.connect

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.core.use
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import dev.datlag.burningseries.k2k.NetInterface
import dev.datlag.tooling.async.suspendCatching
import kotlinx.coroutines.flow.update

internal data object ConnectionServer {
    private var socket = aSocket(SelectorManager(Dispatchers.IO)).tcp()
    private var receiveJob: Job? = null

    fun startServer(
        port: Int,
        scope: CoroutineScope,
        listener: suspend (ByteArray) -> Unit
    ) {
        receiveJob?.cancel()
        receiveJob = scope.launch(Dispatchers.IO) {
            while (true) {
                val socketAddress = InetSocketAddress(NetInterface.getLocalAddress(), port)

                socket.bind(socketAddress) {
                    reuseAddress = true
                    reusePort = true
                }.accept().use { boundSocket ->
                    suspendCatching {
                        val readChannel = boundSocket.openReadChannel()
                        val buffer = ByteArray(readChannel.availableForRead)
                        while (true) {
                            val bytesRead = readChannel.readAvailable(buffer)
                            if (bytesRead <= 0) {
                                break
                            }

                            listener(buffer)
                        }
                    }.onFailure {
                        boundSocket.close()
                    }
                }
            }
        }
    }

    fun stopServer() {
        receiveJob?.cancel()
        socket = aSocket(SelectorManager(Dispatchers.IO)).tcp()
    }
}