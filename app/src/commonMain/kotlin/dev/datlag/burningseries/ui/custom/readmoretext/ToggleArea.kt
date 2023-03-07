package dev.datlag.burningseries.ui.custom.readmoretext

import androidx.compose.runtime.Stable

@JvmInline
public value class ToggleArea private constructor(internal val value: Int) {

    override fun toString(): String {
        return when (this) {
            All -> "All"
            More -> "More"
            else -> "Invalid"
        }
    }

    public companion object {
        /**
         * All area of the text is clickable to toggle.
         */
        @Stable
        public val All: ToggleArea = ToggleArea(1)

        /**
         * 'More' and 'Less' area of the text is clickable to toggle.
         */
        @Stable
        public val More: ToggleArea = ToggleArea(2)
    }
}