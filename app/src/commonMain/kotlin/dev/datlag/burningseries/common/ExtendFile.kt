package dev.datlag.burningseries.common

import java.io.File
import java.nio.file.Files

fun File.createWithParents(): Boolean {
    val exists = runCatching {
        this.exists()
    }.getOrNull() ?: false

    return if (exists) {
        true
    } else {
        runCatching {
            this.parentFile.mkdirs()
        }
        return runCatching {
            this.createNewFile()
        }.getOrNull() ?: false
    }
}

fun File?.existsSafely(): Boolean {
    if (this == null) {
        return false
    }

    return runCatching {
        Files.exists(this.toPath())
    }.getOrNull() ?: runCatching {
        this.exists()
    }.getOrNull() ?: false
}

fun File.canReadSafely(): Boolean {
    return runCatching {
        Files.isReadable(this.toPath())
    }.getOrNull() ?: runCatching {
        this.canRead()
    }.getOrNull() ?: false
}

fun File.content(): String? {
    return runCatching {
        this.readText()
    }.getOrNull()
}