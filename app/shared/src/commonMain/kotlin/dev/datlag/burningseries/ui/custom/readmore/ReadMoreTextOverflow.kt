package dev.datlag.burningseries.ui.custom.readmore

import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@JvmInline
public value class ReadMoreTextOverflow private constructor(internal val value: Int) {

    override fun toString(): String {
        return when (this) {
            Clip -> "Clip"
            Ellipsis -> "Ellipsis"
            else -> "Invalid"
        }
    }

    companion object {
        /**
         * Clip the overflowing text to fix its container.
         */
        @Stable
        val Clip: ReadMoreTextOverflow = ReadMoreTextOverflow(1)

        /**
         * Use an ellipsis to indicate that the text has overflowed.
         */
        @Stable
        val Ellipsis: ReadMoreTextOverflow = ReadMoreTextOverflow(2)
    }
}