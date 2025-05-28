package dev.datlag.mimasu.extension.matcher.wanakana

import dev.datlag.mimasu.extension.matcher.wanakana.common.isHiragana
import dev.datlag.mimasu.extension.matcher.wanakana.common.isJapanese
import dev.datlag.mimasu.extension.matcher.wanakana.common.isKana
import dev.datlag.mimasu.extension.matcher.wanakana.common.isKanji
import dev.datlag.mimasu.extension.matcher.wanakana.common.isKatakana
import dev.datlag.mimasu.extension.matcher.wanakana.common.isRomaji
import dev.datlag.mimasu.extension.matcher.wanakana.common.stripOkurigana
import dev.datlag.mimasu.extension.matcher.wanakana.common.toRomaji
import dev.datlag.mimasu.extension.matcher.wanakana.common.tokenize
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

data object WanaKana {

    /**
     * Returns `true` if [input] is Romaji (allowing Hepburn romanisation).
     *
     * See [Romaji](https://en.wikipedia.org/wiki/Romaji).
     * See [Hepburn romanisation](https://en.wikipedia.org/wiki/Hepburn_romanization).
     *
     * @param allowed additional test allowed to pass for each char.
     *
     * For example:
     * - `isRomaji("Tōkyō and Ōsaka")` => `true`
     * - `isRomaji("12a*b&c-d")` => `true`
     * - `isRomaji("あアA")` => `false`
     * - `isRomaji("お願い")` => `false`
     * - `isRomaji("a！b&cーd")` => `false` // Zenkaku punctuation fails
     * - `isRomaji("a！b&cーd", Regex("""[！ー]"""))` => `true`
     */
    @JvmStatic
    @JvmOverloads
    fun isRomaji(input: String, allowed: Regex? = null): Boolean = input.isRomaji(allowed)

    /**
     * Returns `true` if [input] is Romaji (allowing Hepburn romanisation).
     *
     * See [Romaji](https://en.wikipedia.org/wiki/Romaji).
     * See [Hepburn romanisation](https://en.wikipedia.org/wiki/Hepburn_romanization).
     *
     * For example:
     * - `isRomaji('A')` => `true`
     * - `isRomaji('ō')` => `true`
     * - `isRomaji('あ')` => `false`
     * - `isRomaji('ア')` => `false`
     * - `isRomaji('願')` => `false`
     */
    @JvmStatic
    fun isRomaji(input: Char): Boolean = input.isRomaji()

    /**
     * Returns `true` if [input] only includes Kanji, Kana, zenkaku numbers, and JA
     * punctuation/symbols.
     *
     * See [Kanji](https://en.wikipedia.org/wiki/Kanji).
     * See [Kana](https://en.wikipedia.org/wiki/Kana).
     *
     * @param allowed additional test allowed to pass for each char.
     *
     * For example:
     * - `isJapanese("泣き虫")` => `true`
     * - `isJapanese("あア")` => `true`
     * - `isJapanese("２月")` => `true` // Zenkaku numbers allowed
     * - `isJapanese("泣き虫。！〜＄")` => `true` // Zenkaku/JA punctuation
     * - `isJapanese("泣き虫.!~$")` => `false` // Latin punctuation fails
     * - `isJapanese("A泣き虫")` => `false`
     * - `isJapanese("≪偽括弧≫", Regex("""[≪≫]"""))` => `true`
     */
    @JvmStatic
    @JvmOverloads
    fun isJapanese(input: String, allowed: Regex? = null): Boolean = input.isJapanese(allowed)

    /**
     * Returns `true` if [input] only includes Kanji, Kana, zenkaku numbers, and JA
     * punctuation/symbols.
     *
     * See [Kanji](https://en.wikipedia.org/wiki/Kanji).
     * See [Kana](https://en.wikipedia.org/wiki/Kana).
     *
     * For example:
     * - `isJapanese('泣')` => `true`
     * - `isJapanese('あ')` => `true`
     * - `isJapanese('ア')` => `true`
     * - `isJapanese('２')` => `true` // Zenkaku numbers allowed
     * - `isJapanese('。')` => `true` // JA punctuation
     * - `isJapanese('!')` => `false` // Latin punctuation fails
     * - `isJapanese('A')` => `false`
     */
    @JvmStatic
    fun isJapanese(input: Char): Boolean = input.isJapanese()

    /**
     * Returns `true` if [input] is [Kana](https://en.wikipedia.org/wiki/Kana).
     *
     * For example:
     * - `isKana("あ")` => `true`
     * - `isKana("ア")` => `true`
     * - `isKana("あーア")` => `true`
     * - `isKana("A")` => `false`
     * - `isKana("あAア")` => `false`
     */
    @JvmStatic
    fun isKana(input: String): Boolean = input.isKana()

    /**
     * Returns `true` if [input] is [Kana](https://en.wikipedia.org/wiki/Kana).
     *
     * For example:
     * - `isKana('あ')` => `true`
     * - `isKana('ア')` => `true`
     * - `isKana('A')` => `false`
     */
    @JvmStatic
    fun isKana(input: Char): Boolean = input.isKana()

    /**
     * Returns `true` if [input] is [Hiragana](https://en.wikipedia.org/wiki/Hiragana).
     *
     * For example:
     * - `isHiragana("げーむ")` => `true`
     * - `isHiragana("A")` => `false`
     * - `isHiragana("あア")` => `false`
     */
    @JvmStatic
    fun isHiragana(input: String): Boolean = input.isHiragana()

    /**
     * Returns `true` if [input] is [Hiragana](https://en.wikipedia.org/wiki/Hiragana).
     *
     * For example:
     * - `isHiragana('げ')` => `true`
     * - `isHiragana('A')` => `false`
     * - `isHiragana('ア')` => `false`
     */
    @JvmStatic
    fun isHiragana(input: Char): Boolean = input.isHiragana()

    /**
     * Returns `true` if [input] is [Katakana](https://en.wikipedia.org/wiki/Katakana).
     *
     * For example:
     * - `isKatakana("ゲーム")` => `true`
     * - `isKatakana("あ")` => `false`
     * - `isKatakana("A")` => `false`
     * - `isKatakana("あア")` => `false`
     */
    @JvmStatic
    fun isKatakana(input: String): Boolean = input.isKatakana()

    /**
     * Returns `true` if [input] is [Katakana](https://en.wikipedia.org/wiki/Katakana).
     *
     * For example:
     * - `isKatakana('ア')` => `true`
     * - `isKatakana('あ')` => `false`
     * - `isKatakana('A')` => `false`
     */
    @JvmStatic
    fun isKatakana(input: Char): Boolean = input.isKatakana()

    /**
     * Returns `true` if [input] is [Kanji](https://en.wikipedia.org/wiki/Kanji).
     * See [Japanese CJK ideographs](https://en.wikipedia.org/wiki/CJK_Unified_Ideographs).
     *
     * For example:
     * - `isKanji("刀")` => `true`
     * - `isKanji("切腹")` => `true`
     * - `isKanji("勢い")` => `false`
     * - `isKanji("あAア")` => `false`
     * - `isKanji("")` => `true`
     */
    @JvmStatic
    fun isKanji(input: String): Boolean = input.isKanji()

    /**
     * Returns `true` if [input] is [Kanji](https://en.wikipedia.org/wiki/Kanji).
     * See [Japanese CJK ideographs](https://en.wikipedia.org/wiki/CJK_Unified_Ideographs).
     *
     * For example:
     * - `isKanji('刀')` => `true`
     * - `isKanji('あ')` => `false`
     * - `isKanji('ア')` => `false`
     * - `isKanji('A')` => `false`
     */
    @JvmStatic
    fun isKanji(input: Char): Boolean = input.isKanji()

    /**
     * Converts kana to romaji (Hepburn romanisation).
     *
     * See [Romaji](https://en.wikipedia.org/wiki/Romaji).
     * See [Hepburn romanisation](https://en.wikipedia.org/wiki/Hepburn_romanization).
     *
     * @param input the kana text input.
     * @param imeMode if enabled, handles conversion while the text is being typed, defaults to
     * [IMEMode.DISABLED].
     * @param uppercaseKatakana if `true`, katakana will be converted to uppercase, defaults to
     * `false`.
     * @return the converted text.
     *
     * For example:
     * - `toRomaji("ひらがな　カタカナ")` => `"hiragana katakana"`
     * - `toRomaji("げーむ　ゲーム")` => `"ge-mu geemu"`
     * - `toRomaji("ひらがな　カタカナ", upcaseKatakana = true)` => `"hiragana KATAKANA"`
     */
    @JvmStatic
    fun toRomaji(
        input: String,
        imeMode: IMEMode = IMEMode.DISABLED,
        uppercaseKatakana: Boolean = false
    ): String = input.toRomaji(imeMode, uppercaseKatakana)

    /**
     * Strips [Okurigana](https://en.wikipedia.org/wiki/Okurigana).
     *
     * @param [input] the input text.
     * @param [leading] if `true`, strips leading okurigana instead of trailing okurigana, defaults
     * to `false`.
     * @param [matchKanji] optional kanji representation of the text, to help strip okurigana from a
     * kana input.
     * @return the text with okurigana removed.
     *
     * For example:
     * - `stripOkurigana("踏み込む")` => `"踏み込"`
     * - `stripOkurigana("お祝い")` => `"お祝"`
     * - `stripOkurigana("お腹", leading = true)` => `"腹"`
     * - `stripOkurigana("ふみこむ", matchKanji = "踏み込む")` => `"ふみこ"`
     * - `stripOkurigana("おみまい", matchKanji = "お祝い", leading = true)` => `"みまい"`
     */
    @JvmStatic
    fun stripOkurigana(
        input: String,
        leading: Boolean = false,
        matchKanji: String? = null
    ): String = input.stripOkurigana(leading, matchKanji)
}