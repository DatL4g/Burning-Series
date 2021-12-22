@file:Obfuscate

package de.datlag.model.common

import io.michaelrocks.paranoid.Obfuscate

fun ByteArray.toHex(): String {
    return joinToString(String()) { "%02x".format(it) }
}