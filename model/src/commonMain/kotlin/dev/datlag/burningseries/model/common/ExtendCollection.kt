package dev.datlag.burningseries.model.common

import kotlin.math.max
import kotlin.math.min

fun <T> listFrom(vararg list: Collection<T>): List<T> {
    return mutableListOf<T>().apply {
        list.forEach(::addAll)
    }
}

fun <T> setFrom(vararg list: Collection<T>): Set<T>  {
    return mutableSetOf<T>().apply {
        list.forEach(::addAll)
    }
}

fun <T> T.asList(): List<T> {
    return listOf(this)
}

fun <T> List<T>.safeSubList(from: Int, to: Int): List<T> {
    if (this.isEmpty()) {
        return this
    }

    val safeFrom = max(min(from, lastIndex), 0)
    return this.subList(
        safeFrom,
        max(safeFrom, min(to, size))
    )
}

fun Collection<String>.contains(element: String, ignoreCase: Boolean): Boolean {
    return if (ignoreCase) {
        this.any { it.equals(element, true) }
    } else {
        this.contains(element)
    }
}