package dev.datlag.burningseries.model.common

expect fun String.decodeBase64(): ByteArray
expect fun String.encodeBase64(): String

fun ByteArray.encodeBase64(): String {
    return this.decodeToString().encodeBase64()
}