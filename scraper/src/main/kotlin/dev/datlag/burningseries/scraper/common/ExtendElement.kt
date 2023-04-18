package dev.datlag.burningseries.scraper.common

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

fun Element.getSrc(): String? {
    return if (this.hasAttr("src")) {
        this.attr("src")
    } else {
        val sources = this.select("source")
        sources.firstOrNull()?.getSrc()
    }
}

fun Element.getValue(): String? {
    return if (this.hasAttr("value")) {
        this.attr("value")
    } else {
        null
    }
}
