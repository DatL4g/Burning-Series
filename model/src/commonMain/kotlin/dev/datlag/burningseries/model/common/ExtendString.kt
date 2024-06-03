package dev.datlag.burningseries.model.common

fun String.occurrences(char: Char): Int {
    var counter = 0
    for (c in this) {
        if (c == char) {
            counter++
        }
    }
    return counter
}

fun String.moreThan(char: Char, search: Int): Boolean {
    var counter = 0
    for (c in this) {
        if (c == char) {
            counter++
            if (counter > search) {
                return true
            }
        }
    }
    return counter > search
}