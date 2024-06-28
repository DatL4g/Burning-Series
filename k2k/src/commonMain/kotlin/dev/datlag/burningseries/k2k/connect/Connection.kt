package dev.datlag.burningseries.k2k.connect

import dev.datlag.burningseries.k2k.Host
import dev.datlag.burningseries.k2k.discover.Discovery
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class Connection private constructor(
    private val peers: StateFlow<ImmutableSet<Host>>,
    private val port: Int,
    private val scope: CoroutineScope
) {

    val receiveData: Flow<Pair<Host, ByteArray>> = channelFlow {
        ConnectionServer.receiveData.collectLatest {
            println("Received Data: ${it?.second?.decodeToString()}")

            if (it != null) {
                val peer = peers.value.firstOrNull { aPeer ->
                    aPeer.hostAddress == it.first.substringBefore(':').substringAfter('/')
                }
                peer?.let { p -> send(Pair(p, it.second)) }
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun send(bytes: ByteArray, peer: Host) = ConnectionClient.send(bytes, peer, port)

    fun startReceiving() {
        ConnectionServer.startServer(port, scope)
    }

    fun stopReceiving() {
        ConnectionServer.stopServer()
    }

    class Builder(private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        private var peerFlow: MutableStateFlow<ImmutableSet<Host>> = MutableStateFlow(
            persistentSetOf()
        )
        private var port by Delegates.notNull<Int>()

        fun fromDiscovery(discovery: Discovery) = forPeers(discovery.peers)

        fun forPeers(peers: Flow<Collection<Host>>) = apply {
            scope.launch(Dispatchers.IO) {
                peerFlow.emitAll(peers.map { it.toImmutableSet() })
            }
        }

        fun forPeers(peers: Collection<Host>) = apply {
            peerFlow.update { peers.toImmutableSet() }
        }

        fun forPeer(peer: Host) = forPeers(setOf(peer))

        fun setPort(port: Int) = apply {
            this.port = port
        }

        fun setScope(scope: CoroutineScope) = apply {
            this.scope = scope
        }

        fun build() = Connection(peerFlow.asStateFlow(), port, scope)
    }
}

fun CoroutineScope.connection(builder: Connection.Builder.() -> Unit) = Connection.Builder(this).apply(builder).build()