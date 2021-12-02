@file:Obfuscate

package de.datlag.network.common

import de.datlag.model.common.toHex
import io.michaelrocks.paranoid.Obfuscate
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest

fun String.toMD5(charSet: Charset = Charsets.UTF_8): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray(charSet))
    return bytes.toHex()
}
