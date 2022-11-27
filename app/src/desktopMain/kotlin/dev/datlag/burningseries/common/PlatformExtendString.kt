package dev.datlag.burningseries.common

import java.util.Base64

@Suppress("NewApi")
actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
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
