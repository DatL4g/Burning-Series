package de.datlag.model

import io.michaelrocks.paranoid.Obfuscate
import kotlin.math.max

@Obfuscate
object Levenshtein {

    fun distance(
        s1: String,
        s2: String,
        charScore: (Char, Char) -> Int = { c1, c2 -> if (c1.equals(c2, true)) 0 else 1 }
    ): Int {
        if (s1.equals(s2, true)) {
            return 0
        }
        if (s1.isEmpty()) {
            return s2.length
        }
        if (s2.isEmpty()) {
            return s1.length
        }

        val initialRow: List<Int> = (0 until s2.length + 1).map { it }.toList()
        return (s1.indices).fold(initialRow) { previous, u ->
            (s2.indices).fold(mutableListOf(u + 1)) { row, v ->
                row.add(
                    minOf(
                        (row.last() + 1),
                        (previous[v + 1] + 1),
                        (previous[v] + charScore(s1[u], s2[v]))
                    )
                )
                row
            }
        }.last()
    }

    fun normalizedSimilarity(
        s1: String,
        s2: String,
        charScore: (Char, Char) -> Int = { c1, c2 -> if (c1.equals(c2, true)) 0 else 1 }
    ): Float {
        if (s1.equals(s2, true)) {
            return 1F
        }

        val maxLength = max(s1.length, s2.length)
        if (maxLength == 0) {
            return 1F
        }

        return 1F - (distance(s1, s2, charScore).toFloat() / maxLength.toFloat())
    }
}