package dev.datlag.burningseries.other

import io.ktor.http.*
import okhttp3.*
import java.net.InetAddress
import java.net.UnknownHostException

class MultiDoH internal constructor(
    val dnsServers: List<Dns>
) : Dns {

    private val inetHostMatch: MutableMap<String, Set<InetAddress>> = mutableMapOf()

    constructor(vararg servers: Dns) : this(servers.toList())

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        return inetHostMatch[hostname]?.ifEmpty { null }?.toList() ?: run {
            val failures: MutableList<Exception> = mutableListOf()
            for (i in dnsServers.indices) {
                try {
                    val newList = inetHostMatch.getOrDefault(hostname, emptySet()).toMutableSet().apply {
                        addAll(dnsServers[i].lookup(hostname))
                    }
                    inetHostMatch[hostname] = newList
                } catch (e: Exception) {
                    failures.add(e)
                    continue
                }
            }
            inetHostMatch.getOrDefault(hostname, emptySet()).toList().ifEmpty {
                throwBestFailure(hostname, failures)
            }
        }
    }

    @Throws(UnknownHostException::class)
    private fun throwBestFailure(hostname: String, failures: List<Exception>): List<InetAddress> {
        if (failures.isEmpty()) {
            throw UnknownHostException(hostname)
        }

        val failure = failures[0]

        if (failure is UnknownHostException) {
            throw failure
        }

        val unknownHostException = UnknownHostException(hostname)
        unknownHostException.initCause(failure)

        for (i in 1 until failures.size) {
            unknownHostException.addSuppressed(failures[i])
        }

        throw unknownHostException
    }
}
