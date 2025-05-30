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

    fun tokenize(value: String): TokenResult {
        return tokenRegex.findAll(value).mapNotNull { matchResult ->
            matchResult.value.lowercase().ifBlank { null }?.trim()
        }.distinct().mapIndexed { index, chunk ->
            val isJapanese = WanaKana.isJapanese(chunk)
            val basicTokens = buildSet<TokenResult.Token> {
                if (!isJapanese) {
                    add(TokenResult.Token(chunk, index))
                }
            }
            val extraTokens = buildSet<TokenResult.Token> {
                val hasDash = chunk.any { it == STANDARD_HYPHEN || it == FULL_WIDTH_HYPHEN || it == KANA_LONG_VOWEL }
                val hasLatinChars = chunk.any { it in 'a'..'z' }

                if (hasDash && hasLatinChars) {
                    val sanitizedChunk = chunk.replace(STANDARD_HYPHEN, ' ').replace(FULL_WIDTH_HYPHEN, ' ').replace(KANA_LONG_VOWEL, ' ')
                    addAll(sanitizedChunk.split(' ').filter { it.isNotEmpty() }.map { TokenResult.Token(it, index) })
                }
            }
            val japaneseTokens = buildSet<TokenResult.Token> {
                if (isJapanese) {
                    add(TokenResult.Token(chunk, index))

                    WanaKana.stripOkurigana(chunk).ifBlank { null }?.let {
                        TokenResult.Token(it, index)
                    }?.let(::add)

                    WanaKana.stripOkurigana(chunk, leading = true).ifBlank { null }?.let {
                        TokenResult.Token(it, index)
                    }?.let(::add)
                }
            }
            val japaneseExtraTokens = buildSet<TokenResult.Token> {
                if (isJapanese) {
                    addAll(
                        WanaKana.tokenize(chunk).mapNotNull { it.ifBlank { null } }.map {
                            TokenResult.Token(it, index)
                        }
                    )
                }
            }
            val romajiTokens = buildSet<TokenResult.Token> {
                if (isJapanese) {
                    WanaKana.toRomaji(chunk).ifBlank { null }?.let {
                        TokenResult.Token(it, index)
                    }?.let(::add)
                }
            }

            TokenResult(
                tokens = basicTokens,
                extraTokens = extraTokens,
                japaneseTokens = japaneseTokens,
                japaneseExtraTokens = japaneseExtraTokens,
                romajiTokens = romajiTokens
            )
        }.toSet().fold(TokenResult.Empty) { left, right ->
            left + right
        }
    }

    fun tokenize(vararg multiple: String?): TokenResult {
        return multiple.mapNotNull { it?.ifBlank { null } }.fold(TokenResult.Empty) { left, right ->
            left + tokenize(value = right)
        }
    }
}