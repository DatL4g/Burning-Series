package dev.datlag.burningseries.color.scheme

import dev.datlag.burningseries.color.hct.Hct
import dev.datlag.burningseries.color.palettes.TonalPalette
import dev.datlag.burningseries.color.utils.MathUtils.sanitizeDegreesDouble


open class DynamicScheme(
    val sourceColorHct: Hct,
    val variant: Variant,
    val isDark: Boolean,
    val contrastLevel: Double,
    val primaryPalette: TonalPalette,
    val secondaryPalette: TonalPalette,
    val tertiaryPalette: TonalPalette,
    val neutralPalette: TonalPalette,
    val neutralVariantPalette: TonalPalette
) {
    val sourceColorArgb: Int = sourceColorHct.toInt()
    val errorPalette: TonalPalette = TonalPalette.fromHueAndChroma(25.0, 84.0)

    companion object {

        /**
         * Given a set of hues and set of hue rotations, locate which hues the source color's hue is
         * between, apply the rotation at the same index as the first hue in the range, and return the
         * rotated hue.
         *
         * @param sourceColorHct The color whose hue should be rotated.
         * @param hues A set of hues.
         * @param rotations A set of hue rotations.
         * @return Color's hue with a rotation applied.
         */
        fun getRotatedHue(sourceColorHct: Hct, hues: DoubleArray, rotations: DoubleArray): Double {
            val sourceHue = sourceColorHct.hue
            if (rotations.size == 1) {
                return sanitizeDegreesDouble(sourceHue + rotations[0])
            }
            val size = hues.size
            for (i in 0..size - 2) {
                val thisHue = hues[i]
                val nextHue = hues[i + 1]
                if (thisHue < sourceHue && sourceHue < nextHue) {
                    return sanitizeDegreesDouble(sourceHue + rotations[i])
                }
            }
            // If this statement executes, something is wrong, there should have been a rotation
            // found using the arrays.
            return sourceHue
        }
    }
}