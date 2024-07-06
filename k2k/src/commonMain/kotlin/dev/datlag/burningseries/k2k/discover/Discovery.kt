package dev.datlag.burningseries.k2k.discover

import dev.datlag.burningseries.k2k.Constants
import dev.datlag.burningseries.k2k.Host
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlin.properties.Delegates
import kotlin.time.Duration

class Discovery private constructor(
    private val discoveryTimeout: Long,
    private val discoveryTimeoutListener: (suspend () -> Unit)?,
    private val discoverableTimeout: Long,
    private val discoverableTimeoutListener: (suspend () -> Unit)?,
    private val discoverPing: Long,
    private val port: Int,
    private val hostFilter: Regex,
    private val hostIsClient: Boolean,
    private val scope: CoroutineScope
) {

    private var discoverableTimer: Job? = null
    private var discoveryTimer: Job? = null

    val peers: StateFlow<ImmutableSet<Host>> = DiscoveryServer.hosts

    fun makeDiscoverable(host: Host) {
        val sendDataString = Constants.json.encodeToString(host)
        DiscoveryClient.startBroadcasting(port, discoverPing, sendDataString.encodeToByteArray(), scope)
        discoverableTimer?.cancel()

        if (discoverableTimeout > 0L) {
            discoverableTimer = scope.launch(Dispatchers.IO) {
                delay(discoverableTimeout)
                discoverableTimeoutListener?.invoke()
                stopBeingDiscoverable()
            }
        }
    }

    @JvmOverloads
    fun makeDiscoverable(
        hostName: String,
        filterMatch: String = ""
    ) = makeDiscoverable(Host(hostName, filterMatch))

    fun stopBeingDiscoverable() {
        DiscoveryClient.stopBroadcasting()
        discoverableTimer?.cancel()
    }

    @JvmOverloads
    fun startDiscovery(hostIsClient: Boolean = this.hostIsClient) {
        DiscoveryServer.startListening(port, discoverPing, hostFilter, hostIsClient, scope)
        discoveryTimer?.cancel()

        if (discoveryTimeout > 0L) {
            discoveryTimer = scope.launch(Dispatchers.IO) {
                delay(discoveryTimeout)
                discoveryTimeoutListener?.invoke()
                stopDiscovery()
            }
        }
    }

    fun stopDiscovery() {
        DiscoveryServer.stopListening()
        discoveryTimer?.cancel()
    }

    class Builder(private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        private var discoveryTimeout by Delegates.notNull<Long>()
        private var discoveryTimeoutListener: (suspend () -> Unit)? = null
        private var discoverableTimeout by Delegates.notNull<Long>()
        private var discoverableTimeoutListener: (suspend () -> Unit)? = null
        private var discoverPing: Long = 1000L
        private var port by Delegates.notNull<Int>()
        private var hostFilter: Regex = Regex("^$")
        private var hostIsClientToo = false

        fun setDiscoveryTimeout(timeoutMilli: Long) = apply {
            this.discoveryTimeout = timeoutMilli
        }

        fun setDiscoveryTimeout(duration: Duration) = apply {
            this.discoveryTimeout = duration.inWholeMilliseconds
        }

        fun setDiscoveryTimeoutListener(listener: suspend () -> Unit) = apply {
            this.discoveryTimeoutListener = listener
        }

        fun setDiscoverableTimeout(timeoutMilli: Long) = apply {
            this.discoverableTimeout = timeoutMilli
        }

        fun setDiscoverableTimeout(duration: Duration) = apply {
            this.discoverableTimeout = duration.inWholeMilliseconds
        }

        fun setDiscoverableTimeoutListener(listener: suspend () -> Unit) = apply {
            this.discoverableTimeoutListener = listener
        }

        fun setPing(intervalMilli: Long) = apply {
            this.discoverPing = intervalMilli
        }

        fun setPing(duration: Duration) = apply {
            this.discoverPing = duration.inWholeMilliseconds
        }

        fun setPort(port: Int) = apply {
            this.port = port
        }

        fun setHostFilter(filter: Regex) = apply {
            this.hostFilter = filter
        }

        fun setScope(scope: CoroutineScope) = apply {
            this.scope = scope
        }

        fun setHostIsClient(`is`: Boolean) = apply {
            this.hostIsClientToo = `is`
        }

        fun build() = Discovery(
            discoveryTimeout,
            discoveryTimeoutListener,
            discoverableTimeout,
            discoverableTimeoutListener,
            discoverPing,
            port,
            hostFilter,
            hostIsClientToo,
            scope
        )
    }
}

fun CoroutineScope.discovery(builder: Discovery.Builder.() -> Unit) = Discovery.Builder(this).apply(builder).build()