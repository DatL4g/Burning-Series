package dev.datlag.burningseries.network.bs

expect class Document
expect interface Dns {
    fun lookup(hostname: String): List<InetAddress>
}
expect class InetAddress

expect object BsScraper {
    suspend fun getDocument(url: String, dns: Dns?): Document?

    suspend fun getDocIndex(url: String, dns: Dns?): Int
}