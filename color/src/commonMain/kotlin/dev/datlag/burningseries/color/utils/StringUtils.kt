package dev.datlag.burningseries.color.utils

object StringUtils {

    fun hexFromArgb(argb: Int): String {
        val red = ColorUtils.redFromArgb(argb).toUInt()
        val green = ColorUtils.greenFromArgb(argb).toUInt()
        val blue = ColorUtils.blueFromArgb(argb).toUInt()

        return "#${
            singleDigitToDoubleDigit(
            red.toString(16)
        )
        }${
            singleDigitToDoubleDigit(
            green.toString(16)
        )
        }${
            singleDigitToDoubleDigit(
            blue.toString(16)
        )
        }"
    }

    private fun singleDigitToDoubleDigit(value: String): String {
        return if (value.length == 1) {
            "0$value"
        } else {
            value
        }
    }

}