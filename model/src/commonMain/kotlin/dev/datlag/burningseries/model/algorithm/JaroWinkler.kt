package dev.datlag.burningseries.model.algorithm

import kotlin.math.max
import kotlin.math.min

data object JaroWinkler {
    fun distance(s1: String, s2: String): Double {
        val s1Len = s1.length
        val s2Len = s2.length

        if ((s1Len == 0 && s2Len == 0) || s1.equals(s2, true)) {
            return 1.0
        }

        val matchDistance = max(s1Len, s2Len) / 2 - 1
        val s1Matches = BooleanArray(s1Len)
        val s2Matches = BooleanArray(s2Len)
        var matches = 0

        for (i in 0 until s1Len) {
            val start = max(0, i - matchDistance)
            val end = min(i + matchDistance + 1, s2Len)
            (start until end).find { j -> !s2Matches[j] && s1[i].equals(s2[j], true) }?.let {
                s1Matches[i] = true
                s2Matches[it] = true
                matches++
            }
        }

        if (matches == 0) {
            return 0.0
        }

        var t = 0.0
        var k = 0

        (0 until s1Len).filter { s1Matches[it] }.forEach { i ->
            while (!s2Matches[k]) {
                k++
            }
            if (!s1[i].equals(s2[k], true)) {
                t += 0.5
            }
            k++
        }

        val m = matches.toDouble()
        return (m / s1Len + m / s2Len + (m - t) / m) / 3.0
    }
}