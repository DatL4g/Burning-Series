package dev.datlag.burningseries.network.bs

actual class Document

actual interface Dns {
    actual fun lookup(hostname: String): List<InetAddress>
}
actual class InetAddress

actual object BsScraper {

    actual suspend fun getDocIndex(url: String, dns: Dns?): Int {
        return -1
    }

    actual suspend fun getDocument(
        url: String,
        dns: Dns?
    ): Document? {
        return null
    }
}