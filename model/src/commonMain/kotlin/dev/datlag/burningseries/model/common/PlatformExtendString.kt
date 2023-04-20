package dev.datlag.burningseries.model.common

expect fun String.decodeBase64(): ByteArray
expect fun String.encodeBase64(): String

expect fun ByteArray.encodeBase64(): String