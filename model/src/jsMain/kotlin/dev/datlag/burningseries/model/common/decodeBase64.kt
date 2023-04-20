package dev.datlag.burningseries.model.common

actual fun String.decodeBase64(): ByteArray {
    return ByteArray(0)
}

actual fun String.encodeBase64(): String {
    return String()
}

actual fun ByteArray.encodeBase64(): String {
    return String()
}
