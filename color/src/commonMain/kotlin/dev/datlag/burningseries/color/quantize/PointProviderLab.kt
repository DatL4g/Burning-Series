package dev.datlag.burningseries.color.quantize

import dev.datlag.burningseries.color.utils.ColorUtils.argbFromLab
import dev.datlag.burningseries.color.utils.ColorUtils.labFromArgb


class PointProviderLab : PointProvider {
    /**
     * Convert a color represented in ARGB to a 3-element array of L*a*b* coordinates of the color.
     */
    override fun fromInt(argb: Int): DoubleArray {
        val lab = labFromArgb(argb)
        return doubleArrayOf(lab[0], lab[1], lab[2])
    }

    /** Convert a 3-element array to a color represented in ARGB.  */
    override fun toInt(point: DoubleArray?): Int {
        return argbFromLab(point!![0], point[1], point[2])
    }

    /**
     * Standard CIE 1976 delta E formula also takes the square root, unneeded here. This method is
     * used by quantization algorithms to compare distance, and the relative ordering is the same,
     * with or without a square root.
     *
     *
     * This relatively minor optimization is helpful because this method is called at least once
     * for each pixel in an image.
     */
    override fun distance(a: DoubleArray?, b: DoubleArray?): Double {
        val dL = a!![0] - b!![0]
        val dA = a[1] - b[1]
        val dB = a[2] - b[2]
        return dL * dL + dA * dA + dB * dB
    }
}