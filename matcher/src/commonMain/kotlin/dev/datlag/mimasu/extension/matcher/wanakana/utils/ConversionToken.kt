package dev.datlag.mimasu.extension.matcher.wanakana.utils

internal data class ConversionToken(
    val start: Int,
    val end: Int,
    val value: String?
) {
    val range: IntRange = start until end

    fun shift(amount: Int) = copy(start = start + amount, end = end + amount)

    companion object {
        
    }
}
