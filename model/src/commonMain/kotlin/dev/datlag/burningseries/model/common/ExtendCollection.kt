package dev.datlag.burningseries.model.common

fun <T> Collection<T>.maxSize(size: Int): List<T> {
    return this.toList().subList(0, if (this.size < size) this.size else size)
}