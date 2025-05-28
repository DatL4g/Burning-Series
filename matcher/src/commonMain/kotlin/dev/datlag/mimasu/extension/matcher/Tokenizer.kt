package dev.datlag.mimasu.extension.matcher

import dev.datlag.mimasu.extension.matcher.wanakana.WanaKana

data object Tokenizer {

    private const val KANA_LONG_VOWEL = 'ー'
    private const val FULL_WIDTH_HYPHEN = '－'
    private const val STANDARD_HYPHEN = '-'

    /**
     * Matches any character sequence.
     *
     * Compatible with unicode chars (latin, cyrillic, kana, etc) and numbers.
     */
    private val tokenRegex = "[\\p{L}\\p{N}$KANA_LONG_VOWEL$FULL_WIDTH_HYPHEN$STANDARD_HYPHEN]+".toRegex(
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
    )

    fun tokenize(value: String): List<String> {
        return tokenRegex.findAll(value).mapNotNull { matchResult ->
            val chunk = matchResult.value.lowercase()

            if (chunk.isBlank()) {
                return@mapNotNull null
            }

            buildSet<String> {
                add(chunk)

                if (WanaKana.isJapanese(chunk)) {
                    WanaKana.toRomaji(chunk).ifBlank { null }?.let(::add)
                    WanaKana.stripOkurigana(chunk).ifBlank { null }?.let(WanaKana::toRomaji)?.ifBlank { null }?.let(::add)
                }

                val hasDash = chunk.any { it == STANDARD_HYPHEN || it == FULL_WIDTH_HYPHEN || it == KANA_LONG_VOWEL }
                val hasLatinChars = chunk.any { it in 'a'..'z' }

                if (hasDash && hasLatinChars) {
                    val sanitizedChunk = chunk.replace(STANDARD_HYPHEN, ' ').replace(FULL_WIDTH_HYPHEN, ' ').replace(KANA_LONG_VOWEL, ' ')
                    addAll(sanitizedChunk.split(' ').filter { it.isNotEmpty() })
                }
            }
        }.flatten().distinct().toList()
    }
}