package dev.datlag.burningseries.other

import java.io.InputStream

object EmptyInputStream : InputStream() {

    override fun available(): Int {
        return 0
    }

    override fun read(): Int {
        return -1
    }

    override fun read(b: ByteArray): Int {
        return -1
    }
}