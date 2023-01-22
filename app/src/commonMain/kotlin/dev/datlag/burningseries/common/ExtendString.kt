package dev.datlag.burningseries.common

import dev.datlag.burningseries.model.common.trimHref


fun String.safeDecodeBase64(): ByteArray? = runCatching {
    val decoded = this.decodeBase64()
    if (decoded.isEmpty()) {
        null
    } else {
        decoded
    }
}.getOrNull()

fun String.fileName(): String {
    return this.replace("[^a-zA-Z0-9-_\\.]".toRegex(), "_")
}

fun String.buildTitleHref(): String {
    fun normalizeHref(href: String): String {
        val regex = "serie\\S+".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(href)?.value ?: href
    }

    var newHref = normalizeHref(this)
    if (newHref.startsWith('/')) {
        newHref = newHref.substring(1)
    }
    if (newHref.startsWith("serie/", true) || newHref.startsWith("series/", true)) {
        newHref = newHref.substringAfter('/')
    }
    val hrefSplit = newHref.split('/')

    return hrefSplit[0].trimHref()
}
