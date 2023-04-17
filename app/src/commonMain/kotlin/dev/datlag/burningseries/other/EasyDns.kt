package dev.datlag.burningseries.other

import okhttp3.Dns
import org.xbill.DNS.Address
import org.xbill.DNS.DohResolver
import org.xbill.DNS.ExtendedResolver
import org.xbill.DNS.Lookup
import org.xbill.DNS.SimpleResolver
import java.net.InetAddress

class EasyDns : Dns {

    private var initialized: Boolean = false

    override fun lookup(hostname: String): List<InetAddress> {
        init()
        return try {
            Address.getAllByName(hostname).toList()
        } catch (ignored: Throwable) {
            emptyList()
        }
    }

    private fun init() {
        if (initialized) return

        try {
            val defaultResolver = Lookup.getDefaultResolver()
            val googleFirstResolver = SimpleResolver("8.8.8.8")
            val googleSecondResolver = SimpleResolver("8.8.4.4")
            val cloudflareResolver = SimpleResolver("1.1.1.1")
            val cloudflareDoH = DohResolver("https://cloudflare-dns.com/dns-query")
            val googleDoH = DohResolver("https://dns.google/query")

            Lookup.setDefaultResolver(
                ExtendedResolver(
                    listOf(
                        defaultResolver,
                        googleFirstResolver,
                        googleSecondResolver,
                        cloudflareResolver,
                        cloudflareDoH,
                        googleDoH
                    )
                )
            )
        } catch (ignored: Throwable) { }
        initialized = true
    }
}