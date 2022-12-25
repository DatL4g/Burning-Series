package dev.datlag.burningseries.common


fun String.safeDecodeBase64(): ByteArray? = runCatching {
    val decoded = this.decodeBase64()
    if (decoded.isEmpty()) {
        null
    } else {
        decoded
    }
}.getOrNull()

fun String.trimHref(): String {
    var href = this.trim()
    if(href.startsWith('/')) {
        href = href.substring(1)
    }
    if(href.endsWith('/')) {
        href = href.substring(0, href.length - 2)
    }
    return href.trim()
}

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
