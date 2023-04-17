package dev.datlag.burningseries.network.bs

import dev.datlag.burningseries.network.common.recursiveCount
import dev.datlag.burningseries.network.common.suspendCatching
import io.ktor.http.*
import kotlinx.coroutines.withTimeout
import org.jsoup.Jsoup
import java.net.Socket
import java.net.URI
import kotlin.time.Duration.Companion.seconds

actual typealias Document = org.jsoup.nodes.Document

actual typealias Dns = okhttp3.Dns

actual typealias InetAddress = java.net.InetAddress

actual object BsScraper {

    private var method: METHOD? = null

    actual suspend fun getDocIndex(url: String, dns: Dns?): Int = runCatching {
        getDocument(url, dns)?.recursiveCount() ?: 0
    }.getOrNull() ?: 0

    actual suspend fun getDocument(url: String, dns: Dns?): Document? {
        val addresses = suspendCatching {
            dns?.lookup(Url(url).host)
        }.getOrNull() ?: suspendCatching {
            dns?.lookup(url)
        }.getOrNull() ?: suspendCatching {
            dns?.lookup(URI.create(url).host)
        }.getOrNull() ?: emptyList()

        return try {
            getDocument(url, addresses.toSet().toList())
        } catch (ignored: Throwable) {
            null
        }
    }

    private suspend fun getDocument(url: String, backupAddresses: List<InetAddress> = listOf()): Document? = withTimeout(15.seconds) {
        val host = suspendCatching {
            Url(url).host
        }.getOrNull() ?: suspendCatching {
            URI.create(url).host
        }.getOrNull() ?: url

        suspend fun urlMethod(): Document? {
            return suspendCatching {
                Jsoup.connect(url).followRedirects(true).get()
            }.getOrNull()
        }

        suspend fun ipMethod(ip: InetAddress): Document? {
            val connect = Url(URLBuilder(url).apply {
                set(
                    scheme = URLProtocol.HTTP.name,
                    host = ip.hostAddress?.ifEmpty { ip.hostName } ?: ip.hostName
                )
            }).toString()

            return suspendCatching {
                Jsoup.connect(connect).header("Host", host).followRedirects(true).get()
            }.getOrNull()
        }

        suspend fun socketMethod(ip: InetAddress): Document? = suspendCatching {
            val socket = Socket(ip, 80)
            suspendCatching {
                Jsoup.parse(socket.getInputStream(), null, host)
            }.getOrNull() ?: run {
                val page = String(socket.getInputStream().readBytes())
                Jsoup.parse(page)
            }
        }.getOrNull()

        val (preferredMethod, doc) = when (method) {
            is METHOD.URL -> method to urlMethod()
            is METHOD.IP -> method to ipMethod((method as METHOD.IP).ip)
            is METHOD.Socket -> method to socketMethod((method as METHOD.Socket).ip)
            else -> null to null
        }

        return@withTimeout if (doc == null) {
            var newDoc: Document? = null
            if (preferredMethod == null || preferredMethod is METHOD.URL) {
                backupAddresses.forEach {
                    newDoc = ipMethod(it)
                    if (newDoc != null) {
                        method = METHOD.IP(it)
                        return@forEach
                    }
                }
            }

            if (newDoc == null && (preferredMethod == null || preferredMethod is METHOD.URL)) {
                backupAddresses.forEach {
                    newDoc = socketMethod(it)
                    if (newDoc != null) {
                        method = METHOD.Socket(it)
                        return@forEach
                    }
                }
            }

            if (newDoc == null && (preferredMethod == null || preferredMethod is METHOD.IP)) {
                newDoc = urlMethod()
                if (newDoc != null) {
                    method = METHOD.URL
                }
            }

            if (newDoc == null && (preferredMethod == null || preferredMethod is METHOD.IP)) {
                backupAddresses.forEach {
                    newDoc = socketMethod(it)
                    if (newDoc != null) {
                        method = METHOD.Socket(it)
                        return@forEach
                    }
                }
            }

            if (newDoc == null && (preferredMethod == null || preferredMethod is METHOD.Socket)) {
                newDoc = urlMethod()
                if (newDoc != null) {
                    method = METHOD.URL
                }
            }

            if (newDoc == null && (preferredMethod == null || preferredMethod is METHOD.Socket)) {
                backupAddresses.forEach {
                    newDoc = ipMethod(it)
                    if (newDoc != null) {
                        method = METHOD.IP(it)
                        return@forEach
                    }
                }
            }

            newDoc
        } else {
            doc
        }
    }

    private sealed class METHOD {
        object URL : METHOD()
        data class IP(val ip: InetAddress) : METHOD()
        data class Socket(val ip: InetAddress) : METHOD()
    }
}