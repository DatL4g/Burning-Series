package dev.datlag.burningseries.k2k.discover

import dev.datlag.burningseries.k2k.Constants
import dev.datlag.burningseries.k2k.Host
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import dev.datlag.burningseries.k2k.NetInterface
import dev.datlag.tooling.scopeCatching
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.core.readUTF8Line
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal data object DiscoveryServer {
    private var socket = aSocket(SelectorManager(Dispatchers.IO)).udp()

    private val currentHostIPs = MutableStateFlow<ImmutableSet<String>>(persistentSetOf())
    internal val hosts = MutableStateFlow<ImmutableSet<Host>>(persistentSetOf())
    private var listenJob: Job? = null

    internal fun startListening(
        port: Int,
        ping: Long,
        hostFilter: Regex,
        hostIsClient: Boolean,
        scope: CoroutineScope
    ) {
        listenJob?.cancel()
        listenJob = scope.launch(Dispatchers.IO) {
            updateCurrentDeviceIPs()
            listen(port, ping, hostFilter, hostIsClient)
        }
    }

    internal fun stopListening() {
        listenJob?.cancel()
        socket = aSocket(SelectorManager(Dispatchers.IO)).udp()
        currentHostIPs.update { persistentSetOf() }
        hosts.update { persistentSetOf() }
    }

    private suspend fun updateCurrentDeviceIPs() {
        currentHostIPs.update { NetInterface.getAddresses() }
    }

    private suspend fun listen(port: Int, ping: Long, filter: Regex, hostIsClient: Boolean) {
        val socketAddress = InetSocketAddress(Constants.BROADCAST_SOCKET, port)
        val serverSocket = socket.bind(socketAddress) {
            broadcast = true
            reuseAddress = true
        }

        while (true) {
            serverSocket.openReadChannel()
            serverSocket.incoming.consumeEach { datagram ->
                try {
                    val receivedPacket = datagram.packet.readUTF8Line()
                    if (!receivedPacket.isNullOrBlank()) {
                        val host = Constants.json.decodeFromString<Host>(receivedPacket).apply {
                            val inetSocketAddress = datagram.address as InetSocketAddress
                            this.hostAddress = inetSocketAddress.hostname
                            this.port = inetSocketAddress.port
                        }

                        val keepHosts = hosts.value.toMutableSet()

                        if (hostIsClient || !currentHostIPs.value.contains(host.hostAddress)) {
                            if (host.filterMatch.matches(filter)) {
                                keepHosts.add(host)
                            }
                        }

                        hosts.update { keepHosts.toImmutableSet() }
                    }
                } catch (e: Throwable) {
                    serverSocket.close()
                    if (e is CancellationException) {
                        throw e
                    }
                }
            }

            delay(ping)
        }
    }
}