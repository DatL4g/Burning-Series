package dev.datlag.burningseries.datastore

import java.io.InputStream
import java.io.OutputStream

actual class CryptoManager {

    actual fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        outputStream.write(bytes)
        return bytes
    }

    actual fun decrypt(inputStream: InputStream): ByteArray {
        return inputStream.readBytes()
    }

}