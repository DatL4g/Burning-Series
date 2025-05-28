package dev.datlag.mimasu.extension.matcher.wanakana.common

import dev.datlag.mimasu.extension.matcher.wanakana.Constants

internal fun Char.isHiragana(): Boolean {
    return code in Constants.HIRAGANA_RANGE
}

internal fun Char.isJapanese(): Boolean {
    return Constants.JAPANESE_RANGES.any { range ->
        code in range
    }
}

internal fun Char.isKanji(): Boolean {
    return code in Constants.KANJI_RANGE
}

internal fun Char.isKatakana(): Boolean {
    return code in Constants.KATAKANA_RANGE
}

internal fun Char.isKana(): Boolean {
    return isHiragana() || isKatakana()
}

internal fun Char.isRomaji(): Boolean {
    return Constants.ROMAJI_RANGES.any { range ->
        code in range
    }
}

internal fun Char.isEnSpace(): Boolean {
    return this == ' '
}

internal fun Char.isJaSpace(): Boolean {
    return this == '　'
}

internal fun Char.isJaNum(): Boolean {
    return Constants.jaNumRegex.matches(this.toString())
}

internal fun Char.isEnNum(): Boolean {
    return Constants.enNumRegex.matches(this.toString())
}

internal fun Char.isSlashDot(): Boolean {
    return code == Constants.KANA_SLASH_DOT
}

internal fun Char.isLongDash(): Boolean {
    return code == Constants.PROLONGED_SOUND_MARK
}

internal fun Char.isEnglishUpperCase(): Boolean {
    return code in Constants.LATIN_UPPERCASE_RANGE
}

internal fun Char.isEnglishPunctuation(): Boolean {
    return Constants.EN_PUNCTUATION_RANGES.any { range -> code in range }
}

internal fun Char.isJapanesePunctuation(): Boolean {
    return Constants.JA_PUNCTUATION_RANGES.any { range -> code in range }
}

internal fun Char.isInitialLongDash(index: Int): Boolean = isLongDash() && index < 1
internal fun Char.isInnerLongDash(index: Int): Boolean = isLongDash() && index > 0
internal fun Char.isKanaAsSymbol(): Boolean = this in listOf('ヶ', 'ヵ')