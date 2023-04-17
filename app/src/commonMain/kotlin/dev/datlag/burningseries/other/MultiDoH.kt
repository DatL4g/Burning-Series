package dev.datlag.burningseries.other

import io.ktor.http.*
import okhttp3.*
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException

class MultiDoH internal constructor(
    val dnsServers: List<Dns>
) : Dns {

    constructor(vararg servers: Dns) : this(servers.toList())

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        val inetList: MutableList<InetAddress> = mutableListOf()
        val failures: MutableList<Exception> = mutableListOf()

        val noPostDns = dnsServers.filter { it is DnsOverHttps && !it.post }
        val postDns = dnsServers.filter { it is DnsOverHttps && it.post }
        val otherDns = dnsServers.filter { it !is DnsOverHttps }

        noPostDns.forEach {
            try {
                inetList.addAll(it.lookup(hostname))
            } catch (e: Exception) {
                failures.add(e)
            }
        }

        if (failures.isNotEmpty() || inetList.isEmpty()) {
            otherDns.forEach {
                try {
                    inetList.addAll(it.lookup(hostname))
                } catch (e: Exception) {
                    failures.add(e)
                }
            }
        }

        if (failures.isNotEmpty() || inetList.isEmpty()) {
            postDns.forEach {
                try {
                    inetList.addAll(it.lookup(hostname))
                } catch (e: Exception) {
                    failures.add(e)
                }
            }
        }

        return inetList.toSet().toList().ifEmpty {
            throwBestFailure(hostname, failures)
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
