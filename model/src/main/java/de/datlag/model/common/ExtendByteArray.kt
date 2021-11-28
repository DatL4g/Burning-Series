package de.datlag.model.common

fun ByteArray.toHex(): String {
    return joinToString(String()) { "%02x".format(it) }
}