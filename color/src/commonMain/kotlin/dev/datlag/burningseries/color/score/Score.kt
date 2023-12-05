package dev.datlag.burningseries.color.score

import dev.datlag.burningseries.color.hct.Cam16
import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.utils.ColorUtils.lstarFromArgb
import dev.datlag.burningseries.color.utils.MathUtils.differenceDegrees
import dev.datlag.burningseries.color.utils.MathUtils.sanitizeDegreesInt
import kotlin.math.floor
import kotlin.math.round


object Score {
    private const val CUTOFF_CHROMA = 5.0
    private const val CUTOFF_EXCITED_PROPORTION = 0.01
    private const val CUTOFF_TONE = 10.0
    private const val TARGET_CHROMA = 48.0
    private const val WEIGHT_PROPORTION = 0.7
    private const val WEIGHT_CHROMA_ABOVE = 0.3
    private const val WEIGHT_CHROMA_BELOW = 0.1

    fun score(colorsToPopulation: Map<Int, Int>): List<Int> {
        // Fallback color is Google Blue.
        return score(colorsToPopulation, 4, 0xff4285f4.toInt(), true)
    }

    fun score(colorsToPopulation: Map<Int, Int>, desired: Int): List<Int> {
        return score(colorsToPopulation, desired, 0xff4285f4.toInt(), true)
    }

    fun score(
        colorsToPopulation: Map<Int, Int>, desired: Int, fallbackColorARGB: Int
    ): List<Int> {
        return score(colorsToPopulation, desired, fallbackColorARGB, true)
    }

    /**
     * Given a map with keys of colors and values of how often the color appears, rank the colors
     * based on suitability for being used for a UI theme.
     *
     * @param colorsToPopulation map with keys of colors and values of how often the color appears,
     *     usually from a source image.
     * @param desired max count of colors to be returned in the list.
     * @param fallbackColorARGB color to be returned if no other options available.
     * @param filter whether to filter out undesireable combinations.
     * @return Colors sorted by suitability for a UI theme. The most suitable color is the first item,
     *     the least suitable is the last. There will always be at least one color returned. If all
     *     the input colors were not suitable for a theme, a default fallback color will be provided,
     *     Google Blue.
     */
    fun score(
        colorsToPopulation: Map<Int, Int>,
        desired: Int,
        fallbackColorARGB: Int,
        filter: Boolean
    ): List<Int> {
        // Get the HCT color for each Argb value, while finding the per hue count and
        // total count.
        // Get the HCT color for each Argb value, while finding the per hue count and
        // total count.
        val colorsHct: MutableList<Hct> = mutableListOf()
        val huePopulation = IntArray(360)
        var populationSum = 0.0
        for ((key, value) in colorsToPopulation.entries) {
            val hct = Hct.fromInt(key)
            colorsHct.add(hct)
            val hue: Int = floor(hct.hue).toInt()
            huePopulation[hue] += value
            populationSum += value.toDouble()
        }

        // Hues with more usage in neighboring 30 degree slice get a larger number.

        // Hues with more usage in neighboring 30 degree slice get a larger number.
        val hueExcitedProportions = DoubleArray(360)
        for (hue in 0..359) {
            val proportion = huePopulation[hue] / populationSum
            for (i in hue - 14 until hue + 16) {
                val neighborHue = sanitizeDegreesInt(i)
                hueExcitedProportions[neighborHue] += proportion
            }
        }

        // Scores each HCT color based on usage and chroma, while optionally
        // filtering out values that do not have enough chroma or usage.

        // Scores each HCT color based on usage and chroma, while optionally
        // filtering out values that do not have enough chroma or usage.
        val scoredHcts: MutableList<ScoredHCT> = mutableListOf()
        for (hct in colorsHct) {
            val hue = sanitizeDegreesInt(round(hct.hue).toInt())
            val proportion = hueExcitedProportions[hue]
            if (filter && (hct.chroma < CUTOFF_CHROMA || proportion <= CUTOFF_EXCITED_PROPORTION)) {
                continue
            }
            val proportionScore = proportion * 100.0 * WEIGHT_PROPORTION
            val chromaWeight = if (hct.chroma < TARGET_CHROMA) WEIGHT_CHROMA_BELOW else WEIGHT_CHROMA_ABOVE
            val chromaScore = (hct.chroma - TARGET_CHROMA) * chromaWeight
            val score = proportionScore + chromaScore
            scoredHcts.add(ScoredHCT(hct, score))
        }
        // Sorted so that colors with higher scores come first.
        // Sorted so that colors with higher scores come first.
        scoredHcts.sortWith(ScoredComparator())

        // Iterates through potential hue differences in degrees in order to select
        // the colors with the largest distribution of hues possible. Starting at
        // 90 degrees(maximum difference for 4 colors) then decreasing down to a
        // 15 degree minimum.

        // Iterates through potential hue differences in degrees in order to select
        // the colors with the largest distribution of hues possible. Starting at
        // 90 degrees(maximum difference for 4 colors) then decreasing down to a
        // 15 degree minimum.
        val chosenColors: MutableList<Hct> = mutableListOf()
        for (differenceDegrees in 90 downTo 15) {
            chosenColors.clear()
            for ((hct) in scoredHcts) {
                var hasDuplicateHue = false
                for (chosenHct in chosenColors) {
                    if (differenceDegrees(hct.hue, chosenHct.hue) < differenceDegrees) {
                        hasDuplicateHue = true
                        break
                    }
                }
                if (!hasDuplicateHue) {
                    chosenColors.add(hct)
                }
                if (chosenColors.size >= desired) {
                    break
                }
            }
            if (chosenColors.size >= desired) {
                break
            }
        }
        val colors: MutableList<Int> = mutableListOf()
        if (chosenColors.isEmpty()) {
            colors.add(fallbackColorARGB)
        }
        for (chosenHct in chosenColors) {
            colors.add(chosenHct.toInt())
        }
        return colors
    }

    private fun filter(
        colorsToExcitedProportion: Map<Int, Double>, colorsToCam: Map<Int, Cam16>
    ): List<Int> {
        val filtered: MutableList<Int> = ArrayList()
        for ((color, cam) in colorsToCam) {
            val proportion = colorsToExcitedProportion[color]!!
            if (cam.chroma >= CUTOFF_CHROMA && lstarFromArgb(color) >= CUTOFF_TONE && proportion >= CUTOFF_EXCITED_PROPORTION) {
                filtered.add(color)
            }
        }
        return filtered
    }

    internal class ScoredComparator : Comparator<ScoredHCT> {
        override fun compare(a: ScoredHCT, b: ScoredHCT): Int {
            return b.score.compareTo(a.score)
        }
    }

    data class ScoredHCT(
        val hct: Hct,
        val score: Double
    )
}