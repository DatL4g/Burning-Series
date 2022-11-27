package dev.datlag.burningseries.common


fun String.safeDecodeBase64(): ByteArray? = runCatching {
    val decoded = this.decodeBase64()
    if (decoded.isEmpty()) {
        null
    } else {
        decoded
    }
}.getOrNull()
