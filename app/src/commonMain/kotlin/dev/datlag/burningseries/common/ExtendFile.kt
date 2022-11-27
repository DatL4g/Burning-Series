package dev.datlag.burningseries.common

import java.io.File

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