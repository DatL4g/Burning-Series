@file:Obfuscate

package de.datlag.burningseries.common

import io.michaelrocks.paranoid.Obfuscate

fun Collection<*>.isLargerThan(size: Int) = this.size > size

fun <T> List<T>.safeSubList(startIndex: Int = 0, endIndex: Int): List<T> {
    val safeStart = if (startIndex > this.size - 1) {
        if (this.isNotEmpty()) {
            this.size - 1
        } else {
            0
        }
    } else {
        startIndex
    }
    val safeEnd = if (endIndex > this.size - 1) {
        if (this.isNotEmpty()) {
            this.size
        } else {
            0
        }
    } else {
        endIndex
    }
    return this.subList(safeStart, safeEnd)
}