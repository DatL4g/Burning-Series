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

inline fun <T> List<T>.indexOfLastWithItem(predicate: (T) -> Boolean): Pair<Int, T?> {
    val iterator = this.listIterator(size)
    while (iterator.hasPrevious()) {
        if (predicate(iterator.previous())) {
            return Pair(iterator.nextIndex(), iterator.next())
        }
    }
    return Pair(-1, null)
}