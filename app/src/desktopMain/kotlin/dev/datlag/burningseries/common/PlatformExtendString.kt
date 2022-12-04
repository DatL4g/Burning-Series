package dev.datlag.burningseries.common

import java.util.Base64

@Suppress("NewApi")
actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

@Suppress("NewApi")
actual fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}

fun systemProperty(key: String): String? = runCatching {
    System.getProperty(key).ifEmpty {
        null
    }
}.getOrNull()

fun systemEnv(key: String): String? = runCatching {
    System.getenv(key).ifEmpty {
        null
    }
}.getOrNull()
