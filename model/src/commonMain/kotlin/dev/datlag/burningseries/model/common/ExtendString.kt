package dev.datlag.burningseries.model.common

fun String.getDigitsOrNull(): String? {
    val replaced = this.replace("\\D+".toRegex(), String())
    return replaced.ifEmpty {
        null
    }
}

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
