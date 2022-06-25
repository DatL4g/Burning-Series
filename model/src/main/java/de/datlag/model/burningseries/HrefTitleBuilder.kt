package de.datlag.model.burningseries

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
abstract class HrefTitleBuilder {
    abstract val href: String

    open fun hrefTitleFallback(): String {
        return String()
    }

    fun getHrefTitle(): String {
        val noHttps = if (href.startsWith("https://")) {
            href.substring(8)
        } else if (href.startsWith("http://")) {
            href.substring(7)
        } else { href }
        val noBs = if (noHttps.startsWith("bs.to")) {
            noHttps.substring(5)
        } else { noHttps }
        val normHref = if (noBs.startsWith("/")) {
            noBs.substring(1)
        } else { noBs }
        val match = Regex("(/(\\w|-)+)").find(normHref)
        return match?.groupValues?.getOrNull(1)?.replace("/", "") ?: hrefTitleFallback()
    }
}
