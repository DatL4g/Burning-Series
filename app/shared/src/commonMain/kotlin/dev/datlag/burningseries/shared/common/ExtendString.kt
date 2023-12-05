package dev.datlag.burningseries.shared.common

import androidx.compose.ui.text.AnnotatedString
import kotlin.math.max
import kotlin.math.min

fun AnnotatedString.safeSubSequence(from: Int, to: Int): AnnotatedString {
    if (this.isEmpty()) {
        return this
    }

    val safeFrom = max(min(from, lastIndex), 0)
    return this.subSequence(
        safeFrom,
        max(safeFrom, min(to, length))
    )
}