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

fun ByteArray.toHexString(): String {
    fun doubleDigit(value: String): String {
        return if (value.length == 1) {
            "0$value"
        } else {
            value
        }
    }

    val builder = StringBuilder()
    this.forEach {
        builder.append(doubleDigit((it.toInt() and 0xFF).toString(16)))
    }
    return builder.toString()
}
