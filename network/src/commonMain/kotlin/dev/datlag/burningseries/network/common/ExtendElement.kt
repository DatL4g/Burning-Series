package dev.datlag.burningseries.network.common

import dev.datlag.burningseries.model.common.scopeCatching
import ktsoup.KtSoupElement

fun KtSoupElement.hasAttr(key: String): Boolean {
    return this.attrs().keys.contains(key)
}

fun KtSoupElement.getTitle(): String {
    return this.attr("title")?.ifBlank { null } ?: this.textContent()
}

fun KtSoupElement.getHref(): String? {
    return this.attr("href")?.ifBlank { null }
}

fun KtSoupElement.getSrc(): String? {
    return this.attr("src")?.ifBlank { null } ?: run {
        val sources = this.querySelectorAll("source")
        sources.firstOrNull()?.getSrc()
    }
}

fun KtSoupElement.getSources(): Set<String> {
    return setOfNotNull(
        this.attr("src"),
        *(scopeCatching {
            this.querySelectorAll("source").map { it.getSources() }.flatten().toTypedArray()
        }.getOrNull() ?: emptyArray())
    )
}

fun KtSoupElement.getValue(): String? {
    return this.attr("value")?.ifBlank { null }
}