package dev.datlag.burningseries.color.hct

import dev.datlag.burningseries.color.utils.ColorUtils.lstarFromArgb
import dev.datlag.burningseries.color.utils.ColorUtils.lstarFromY


class Hct private constructor(argb: Int) {
    var hue = 0.0
        private set
    var chroma = 0.0
        private set
    var tone = 0.0
        private set
    var argb = 0
        private set

    init {
        setInternalState(argb)
    }

    fun toInt(): Int {
        return argb
    }

    /**
     * Set the hue of this color. Chroma may decrease because chroma has a different maximum for any
     * given hue and tone.
     *
     * @param newHue 0 <= newHue < 360; invalid values are corrected.
     */
    fun setHue(newHue: Double) {
        setInternalState(HctSolver.solveToInt(newHue, chroma, tone))
    }

    /**
     * Set the chroma of this color. Chroma may decrease because chroma has a different maximum for
     * any given hue and tone.
     *
     * @param newChroma 0 <= newChroma < ?
     */
    fun setChroma(newChroma: Double) {
        setInternalState(HctSolver.solveToInt(hue, newChroma, tone))
    }

    /**
     * Set the tone of this color. Chroma may decrease because chroma has a different maximum for any
     * given hue and tone.
     *
     * @param newTone 0 <= newTone <= 100; invalid valids are corrected.
     */
    fun setTone(newTone: Double) {
        setInternalState(HctSolver.solveToInt(hue, chroma, newTone))
    }

    /**
     * Translate a color into different ViewingConditions.
     *
     *
     * Colors change appearance. They look different with lights on versus off, the same color, as
     * in hex code, on white looks different when on black. This is called color relativity, most
     * famously explicated by Josef Albers in Interaction of Color.
     *
     *
     * In color science, color appearance models can account for this and calculate the appearance
     * of a color in different settings. HCT is based on CAM16, a color appearance model, and uses it
     * to make these calculations.
     *
     *
     * See ViewingConditions.make for parameters affecting color appearance.
     */
    fun inViewingConditions(vc: ViewingConditions?): Hct {
        // 1. Use CAM16 to find XYZ coordinates of color in specified VC.
        val cam16 = Cam16.fromInt(toInt())
        val viewedInVc = cam16.xyzInViewingConditions(vc!!, null)

        // 2. Create CAM16 of those XYZ coordinates in default VC.
        val recastInVc = Cam16.fromXyzInViewingConditions(
            viewedInVc[0], viewedInVc[1], viewedInVc[2], ViewingConditions.DEFAULT
        )

        // 3. Create HCT from:
        // - CAM16 using default VC with XYZ coordinates in specified VC.
        // - L* converted from Y in XYZ coordinates in specified VC.
        return from(
            recastInVc.hue, recastInVc.chroma, lstarFromY(viewedInVc[1])
        )
    }

    private fun setInternalState(argb: Int) {
        this.argb = argb
        val cam = Cam16.fromInt(argb)
        hue = cam.hue
        chroma = cam.chroma
        tone = lstarFromArgb(argb)
    }

    companion object {
        /**
         * Create an HCT color from hue, chroma, and tone.
         *
         * @param hue 0 <= hue < 360; invalid values are corrected.
         * @param chroma 0 <= chroma < ?; Informally, colorfulness. The color returned may be lower than
         * the requested chroma. Chroma has a different maximum for any given hue and tone.
         * @param tone 0 <= tone <= 100; invalid values are corrected.
         * @return HCT representation of a color in default viewing conditions.
         */
        fun from(hue: Double, chroma: Double, tone: Double): Hct {
            val argb: Int = HctSolver.solveToInt(hue, chroma, tone)
            return Hct(argb)
        }

        /**
         * Create an HCT color from a color.
         *
         * @param argb ARGB representation of a color.
         * @return HCT representation of a color in default viewing conditions
         */
        fun fromInt(argb: Int): Hct {
            return Hct(argb)
        }
    }
}