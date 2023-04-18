package dev.datlag.burningseries.model.common

import android.util.Base64

actual fun String.decodeBase64(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}

actual fun String.encodeBase64(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.DEFAULT)
}
