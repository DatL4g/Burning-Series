package dev.datlag.mimasu.extension.matcher.wanakana.common

import dev.datlag.mimasu.extension.matcher.wanakana.utils.ConversionToken

internal fun IntRange.matchSelection(tokens: List<ConversionToken>): IntRange {
    fun correctRange(newFirst: Int, newLast: Int): IntRange {
        return if (first == last) {
            newLast..newLast
        } else {
            newFirst..newFirst
        }
    }

    if (first < 0 || last < 0) {
        return -1..-1
    }
    if (this == 0..0 || tokens.isEmpty()) {
        return 0..0
    }

    var currentSize = 0
    var newFirst = -1

    tokens.forEach { token ->
        val tokenStart = token.range.first
        val tokenEnd = token.end

        if (newFirst == -1 && first < tokenEnd) {
            newFirst = currentSize
        }
        if (last == tokenStart) {
            return correctRange(newFirst, currentSize)
        }

        currentSize += token.value?.length ?: (tokenEnd - tokenStart)

        if (last < tokenEnd) {
            return correctRange(newFirst, currentSize)
        }
    }

    return if (newFirst != -1) {
        correctRange(newFirst, currentSize)
    } else {
        currentSize..currentSize
    }
}