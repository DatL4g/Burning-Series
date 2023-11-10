package dev.datlag.burningseries.color.quantize


internal interface Quantizer {
    fun quantize(pixels: IntArray?, maxColors: Int): QuantizerResult
}