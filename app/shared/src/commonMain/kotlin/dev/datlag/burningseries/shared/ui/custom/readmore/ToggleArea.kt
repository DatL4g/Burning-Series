package dev.datlag.burningseries.shared.ui.custom.readmore

import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline

@JvmInline
value class ToggleArea private constructor(internal val value: Int) {

    override fun toString(): String {
        return when (this) {
            All -> "All"
            More -> "More"
            else -> "Invalid"
        }
    }

    companion object {
        /**
         * All area of the text is clickable to toggle.
         */
        @Stable
        val All: ToggleArea = ToggleArea(1)

        /**
         * 'More' and 'Less' area of the text is clickable to toggle.
         */
        @Stable
        val More: ToggleArea = ToggleArea(2)
    }
}