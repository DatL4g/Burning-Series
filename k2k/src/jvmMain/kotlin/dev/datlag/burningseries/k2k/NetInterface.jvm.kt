package dev.datlag.burningseries.k2k

import dev.datlag.tooling.scopeCatching
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import java.net.InetAddress
import java.net.NetworkInterface

actual object NetInterface {
    actual fun getAddresses(): ImmutableSet<String> {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        val updatedIPs = mutableSetOf<String>()

        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            try {
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                networkInterface.interfaceAddresses.forEach {
                    if (it.broadcast != null) {
                        updatedIPs.add(it.broadcast.hostAddress)
                    }
                }
            } catch (e: Throwable) {
                if (e is CancellationException) {
                    throw e
                }
            }
        }

        return updatedIPs.toImmutableSet()
    }

    actual fun getLocalAddress(): String {
        return InetAddress.getLocalHost().hostAddress
    }
}