package dev.datlag.burningseries.network.common

import org.jsoup.nodes.Element

fun Element.getTitle(): String {
    return if (this.hasAttr("title")) {
        this.attr("title")
    } else {
        this.text()
    } ?: String()
}

fun Element.getHref(): String? {
    return if (this.hasAttr("href")) {
        this.attr("href")
    } else {
        null
    }
}

fun Element.getSources(): List<String> {
    return if (this.hasAttr("src")) {
        listOf(this.attr("src"))
    } else {
        val sources = this.select("source")
        sources.map { it.getSources() }.flatten()
    }
}