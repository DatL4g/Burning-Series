package dev.datlag.mimasu.extension.matcher.wanakana.common

import dev.datlag.mimasu.extension.matcher.wanakana.Constants
import dev.datlag.mimasu.extension.matcher.wanakana.IMEMode
import dev.datlag.mimasu.extension.matcher.wanakana.conversion.KatakanaToHiragana
import dev.datlag.mimasu.extension.matcher.wanakana.utils.ConversionToken
import dev.datlag.mimasu.extension.matcher.wanakana.utils.IMEText
import dev.datlag.mimasu.extension.matcher.wanakana.utils.MappingTree
import dev.datlag.mimasu.extension.matcher.wanakana.utils.State
import dev.datlag.mimasu.extension.matcher.wanakana.utils.TypedToken

internal fun String.isHiragana(): Boolean {
    return all { char -> char.isHiragana() }
}

internal fun String.isJapanese(allowed: Regex? = null): Boolean {
    return all { char ->
        char.isJapanese() || (allowed?.matches(char.toString()) == true)
    }
}

internal fun String.isKanji(): Boolean {
    return all { char ->
        char.isKanji()
    }
}

internal fun String.isKatakana(): Boolean {
    return all { char ->
        char.isKatakana()
    }
}

internal fun String.isKana(): Boolean {
    return all { char ->
        char.isKana()
    }
}

internal fun String.isRomaji(allowed: Regex? = null): Boolean {
    return all { char ->
        char.isRomaji() || (allowed?.matches(char.toString()) == true)
    }
}

internal fun String.tokenize(compact: Boolean = false): List<String> {
    return TypedToken.tokenize(this, compact).map(TypedToken::value)
}

internal fun String.applyMapping(
    map: MappingTree,
    convertEnding: Boolean
): List<ConversionToken> {
    val state = State(map, convertEnding)
    return state.newChunk(this, 0)
}

internal fun String.toRomaji(
    mode: IMEMode = IMEMode.DISABLED,
    uppercaseKatakana: Boolean = false
): String {
    val dummyImeText = IMEText(this, selection = -1..-1)
    return dummyImeText.toRomaji(mode, uppercaseKatakana).text
}

internal fun String.katakanaToHiragana(isDestinationRomaji: Boolean = false): String {
    var previousKana: Char? = null
    return buildString {
        this@katakanaToHiragana.forEachIndexed { index, char ->
            if (char.isSlashDot() || char.isInitialLongDash(index) || char.isKanaAsSymbol()) {
                append(char)
            } else if (previousKana != null && char.isInnerLongDash(index)) {
                val romaji = previousKana.toString().toRomaji().last()

                if (this@katakanaToHiragana[index - 1].isKatakana() && romaji == 'o' && isDestinationRomaji) {
                    append('お')
                } else {
                    KatakanaToHiragana.LONG_VOWELS[romaji]?.let { append(it) }
                }
            } else if (!char.isLongDash() && char.isKatakana()) {
                val code = char.code + (Constants.HIRAGANA_START - Constants.KATAKANA_START)
                val hiraChar = code.toChar()
                previousKana = hiraChar
                append(hiraChar)
            } else {
                previousKana = null
                append(char)
            }
        }
    }
}

private fun String.isLeadingWithoutInitialKana(leading: Boolean): Boolean {
    return leading && !this.first().isKana()
}

private fun String.isTrailingWithoutFinalKana(leading: Boolean): Boolean {
    return !leading && !this.last().isKana()
}

private fun String.isInvalidMatcher(matchKanji: String? = null): Boolean {
    return matchKanji?.any(Char::isKanji) == false || (matchKanji == null && isKana())
}

internal fun String.stripOkurigana(
    leading: Boolean = false,
    matchKanji: String? = null
): String {
    if (this.isEmpty() || !isJapanese() || isLeadingWithoutInitialKana(leading) || isTrailingWithoutFinalKana(leading) || isInvalidMatcher(matchKanji)) {
        return this
    }

    val chars = matchKanji?.takeUnless(String::isBlank) ?: this
    val pattern = if (leading) {
        "^${chars.tokenize().first()}"
    } else {
        "${chars.tokenize().last()}$"
    }
    val okuriganaRegex = Regex(pattern)
    return okuriganaRegex.replace(this, "")
}