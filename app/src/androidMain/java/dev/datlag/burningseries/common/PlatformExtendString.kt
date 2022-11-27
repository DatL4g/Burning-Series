package dev.datlag.burningseries.common

import android.util.Base64

actual fun String.decodeBase64(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}