@file:Suppress("NewApi")

package dev.datlag.burningseries.model.common

import java.util.*


actual fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

actual fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray())
}
