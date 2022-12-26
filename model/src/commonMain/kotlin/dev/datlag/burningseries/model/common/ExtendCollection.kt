package dev.datlag.burningseries.model.common

fun <T> Collection<T>.maxSize(size: Int): List<T> {
    return this.toList().subList(0, if (this.size < size) this.size else size)
}

fun <T> MutableList<T>.move(from: Int, to: Int): MutableList<T> {
    if (to > from) {
        for (i in from until to) {
            this[i] = this[i + 1].also { this[i + 1] = this[i] }
        }
    } else {
        for (i in from downTo to + 1) {
            this[i] = this[i - 1].also { this[i - 1] = this[i] }
        }
    }
    return this
}