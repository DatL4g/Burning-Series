package de.datlag.model.common

import android.util.Base64

fun String.base64ToByteArray() = Base64.decode(this.substringAfterLast(','), Base64.DEFAULT)