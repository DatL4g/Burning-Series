package dev.datlag.mimasu.extension.matcher.wanakana.utils

import dev.datlag.mimasu.extension.matcher.wanakana.IMEMode
import dev.datlag.mimasu.extension.matcher.wanakana.common.applyMapping
import dev.datlag.mimasu.extension.matcher.wanakana.common.isKatakana
import dev.datlag.mimasu.extension.matcher.wanakana.common.katakanaToHiragana
import dev.datlag.mimasu.extension.matcher.wanakana.common.matchSelection
import dev.datlag.mimasu.extension.matcher.wanakana.conversion.KanaToRomaji

internal data class IMEText(
    val text: String,
    val selection: IntRange
) {

    constructor(text: String, start: Int, end: Int) : this(text, start..end)

    fun toRomaji(
        imeMode: IMEMode = IMEMode.ENABLED,
        uppercaseKatakana: Boolean = false
    ): IMEText {
        if (text.isEmpty()) {
            return this
        }

        val tokens = splitIntoRomaji(text, imeMode)
        val newSelection = selection.matchSelection(tokens)
        val newText = tokens.joinToString(separator = "") { token ->
            val romaji = token.value
            if (romaji == null) {
                text.slice(token.range)
            } else {
                val makeUpperCase = uppercaseKatakana && text.slice(token.range).isKatakana()
                if (makeUpperCase) {
                    romaji.uppercase()
                } else {
                    romaji
                }
            }
        }

        return IMEText(newText, newSelection)
    }

    private fun splitIntoRomaji(input: String, imeMode: IMEMode): List<ConversionToken> {
        val map = KanaToRomaji.kanaToHepburnMap
        val hiragana = input.katakanaToHiragana(true)
        return hiragana.applyMapping(map, imeMode == IMEMode.DISABLED)
    }
}