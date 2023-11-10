package dev.datlag.burningseries.color.dislike

import dev.datlag.burningseries.color.hct.Hct
import kotlin.math.roundToInt


object DislikeAnalyzer {

    /**
     * Returns true if color is disliked.
     *
     *
     * Disliked is defined as a dark yellow-green that is not neutral.
     */
    fun isDisliked(hct: Hct): Boolean {
        val huePasses = hct.hue.roundToInt() >= 90.0 && hct.hue.roundToInt() <= 111.0
        val chromaPasses = hct.chroma.roundToInt() > 16.0
        val tonePasses = hct.tone.roundToInt() < 65.0
        return huePasses && chromaPasses && tonePasses
    }

    /** If color is disliked, lighten it to make it likable.  */
    fun fixIfDisliked(hct: Hct): Hct {
        return if (isDisliked(hct)) {
            Hct.from(hct.hue, hct.chroma, 70.0)
        } else hct
    }
}