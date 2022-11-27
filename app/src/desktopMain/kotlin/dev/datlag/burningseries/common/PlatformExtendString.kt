package dev.datlag.burningseries.common

import java.util.Base64

@Suppress("NewApi")
actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}