package dev.datlag.burningseries.color.quantize


object QuantizerCelebi {
    /**
     * Reduce the number of colors needed to represented the input, minimizing the difference between
     * the original image and the recolored image.
     *
     * @param pixels Colors in ARGB format.
     * @param maxColors The number of colors to divide the image into. A lower number of colors may be
     * returned.
     * @return Map with keys of colors in ARGB format, and values of number of pixels in the original
     * image that correspond to the color in the quantized image.
     */
    fun quantize(pixels: IntArray, maxColors: Int): Map<Int, Int> {
        val wu = QuantizerWu()
        val wuResult: QuantizerResult = wu.quantize(pixels, maxColors)
        val wuClustersAsObjects = wuResult.colorToCount.keys
        var index = 0
        val wuClusters = IntArray(wuClustersAsObjects.size)
        for (argb in wuClustersAsObjects) {
            wuClusters[index++] = argb
        }
        return QuantizerWsmeans.quantize(pixels, wuClusters, maxColors)
    }
}