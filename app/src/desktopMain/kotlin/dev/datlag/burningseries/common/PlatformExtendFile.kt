@file:Suppress("NewApi")

package dev.datlag.burningseries.common

import java.io.File
import java.nio.file.Files

fun File.safeExists(): Boolean {
    return runCatching {
        Files.exists(this.toPath())
    }.getOrNull() ?: runCatching {
        this.exists()
    }.getOrNull() ?: false
}

fun File.safeReadable(): Boolean {
    return runCatching {
        Files.isReadable(this.toPath())
    }.getOrNull() ?: runCatching {
        this.canRead()
    }.getOrNull() ?: false
}

fun File.safeWriteable(): Boolean {
    return runCatching {
        Files.isWritable(this.toPath())
    }.getOrNull() ?: runCatching {
        this.canWrite()
    }.getOrNull() ?: false
}

fun File.safeMkdir(): Boolean {
    return runCatching {
        Files.createDirectory(this.toPath())
    }.getOrNull()?.toFile()?.safeExists() ?: runCatching {
        this.mkdir()
    }.getOrNull() ?: false
}

fun File.safeMkdirs(): Boolean {
    return runCatching {
        Files.createDirectories(this.toPath())
    }.getOrNull()?.toFile()?.safeExists() ?: runCatching {
        this.mkdirs()
    }.getOrNull() ?: false
}

fun File.existsAndAccessible(mustWrite: Boolean = true): Boolean {
    if (!safeExists()) {
        return false
    }

    if (!safeReadable()) {
        return false
    }

    return if (mustWrite) {
        safeWriteable()
    } else {
        true
    }
}

fun File.existsOrCreateDirectory(): Boolean {
    if (this.existsAndAccessible(true)) {
        return true
    }

    if (!safeExists()) {
        if (!safeMkdirs()) {
            safeMkdir()
        }
    }

    return this.existsAndAccessible(true)
}