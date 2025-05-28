package dev.datlag.mimasu.extension.matcher.wanakana

internal data object Constants {

    private const val LATIN_LOWERCASE_START = 0x61
    private const val LATIN_LOWERCASE_END = 0x7a
    private const val LATIN_UPPERCASE_START = 0x41
    private const val LATIN_UPPERCASE_END = 0x5a
    private const val LOWERCASE_ZENKAKU_START = 0xff41
    private const val LOWERCASE_ZENKAKU_END = 0xff5a
    private const val UPPERCASE_ZENKAKU_START = 0xff21
    private const val UPPERCASE_ZENKAKU_END = 0xff3a
    const val HIRAGANA_START = 0x3041
    private const val HIRAGANA_END = 0x3096
    const val KATAKANA_START = 0x30a1
    private const val KATAKANA_END = 0x30fc
    private const val KANJI_START = 0x4e00
    private const val KANJI_END = 0x9faf
    const val PROLONGED_SOUND_MARK = 0x30fc
    const val KANA_SLASH_DOT = 0x30fb

    val ZENKAKU_NUMBERS = 0xff10..0xff19
    val ZENKAKU_PUNCTUATION_1 = 0xff01..0xff0f
    val ZENKAKU_PUNCTUATION_2 = 0xff1a..0xff1f
    val ZENKAKU_PUNCTUATION_3 = 0xff3b..0xff3f
    val ZENKAKU_PUNCTUATION_4 = 0xff5b..0xff60
    val ZENKAKU_SYMBOLS_CURRENCY = 0xffe0..0xffee

    val HIRAGANA_CHARS = 0x3040..0x309f
    val KATAKANA_CHARS = 0x30a0..0x30ff
    val HANKAKU_KATAKANA = 0xff66..0xff9f
    val KATAKANA_PUNCTUATION = 0x30fb..0x30fc
    val KANA_PUNCTUATION = 0xff61..0xff65
    val CJK_SYMBOLS_PUNCTUATION = 0x3000..0x303f
    val COMMON_CJK = 0x4e00..0x9fff
    val RARE_CJK = 0x3400..0x4dbf

    val LATIN_LOWERCASE_RANGE = LATIN_LOWERCASE_START..LATIN_LOWERCASE_END
    val LATIN_UPPERCASE_RANGE = LATIN_UPPERCASE_START..LATIN_UPPERCASE_END
    val HIRAGANA_RANGE = HIRAGANA_START..HIRAGANA_END
    val KATAKANA_RANGE = KATAKANA_START..KATAKANA_END
    val KANJI_RANGE = KANJI_START..KANJI_END
    val ZENKAKU_UPPERCASE_RANGE = UPPERCASE_ZENKAKU_START..UPPERCASE_ZENKAKU_END
    val ZENKAKU_LOWERCASE_RANGE = LOWERCASE_ZENKAKU_START..LOWERCASE_ZENKAKU_END

    val KANA_RANGES = arrayOf<IntRange>(
        HIRAGANA_CHARS,
        KATAKANA_CHARS,
        KANA_PUNCTUATION,
        HANKAKU_KATAKANA
    )

    val JA_PUNCTUATION_RANGES = arrayOf<IntRange>(
        CJK_SYMBOLS_PUNCTUATION,
        KANA_PUNCTUATION,
        KATAKANA_PUNCTUATION,
        ZENKAKU_PUNCTUATION_1,
        ZENKAKU_PUNCTUATION_2,
        ZENKAKU_PUNCTUATION_3,
        ZENKAKU_PUNCTUATION_4,
        ZENKAKU_SYMBOLS_CURRENCY
    )

    // Includes kanji, kana, zenkaku latin chars, punctuation, and number ranges.
    val JAPANESE_RANGES = arrayOf<IntRange>(
        *KANA_RANGES,
        *JA_PUNCTUATION_RANGES,
        ZENKAKU_UPPERCASE_RANGE,
        ZENKAKU_LOWERCASE_RANGE,
        ZENKAKU_NUMBERS,
        COMMON_CJK,
        RARE_CJK
    )

    val MODERN_ENGLISH = 0x0000..0x007f
    val HEPBURN_MACRON_RANGES = arrayOf<IntRange>(
        0x0100..0x0101, // Ā ā
        0x0112..0x0113, // Ē ē
        0x012a..0x012b, // Ī ī
        0x014c..0x014d, // Ō ō
        0x016a..0x016b // Ū ū
    )
    val SMART_QUOTE_RANGES = arrayOf<IntRange>(
        0x2018..0x2019, // ‘ ’
        0x201c..0x201d // “ ”
    )

    val ROMAJI_RANGES = arrayOf(MODERN_ENGLISH, *HEPBURN_MACRON_RANGES)
    val EN_PUNCTUATION_RANGES = arrayOf<IntRange>(
        0x20..0x2f,
        0x3a..0x3f,
        0x5b..0x60,
        0x7b..0x7e,
        *SMART_QUOTE_RANGES
    )

    val jaNumRegex: Regex by lazy(LazyThreadSafetyMode.NONE) { Regex("[０-９]") }
    val enNumRegex: Regex by lazy(LazyThreadSafetyMode.NONE) { Regex("[0-9]") }
}