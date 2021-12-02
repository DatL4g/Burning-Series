@file:Obfuscate

package de.datlag.model.common

import android.util.Base64
import io.michaelrocks.paranoid.Obfuscate
import java.security.MessageDigest

fun String.base64ToByteArray() = Base64.decode(this.substringAfterLast(','), Base64.DEFAULT)
