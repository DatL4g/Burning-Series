package dev.datlag.mimasu.extension.matcher

data class TokenResult(
    val tokens: Collection<Token>,
    val extraTokens: Collection<Token>,
    val japaneseTokens: Collection<Token>,
    val japaneseExtraTokens: Collection<Token>,
    val romajiTokens: Collection<Token>,
) {

    operator fun plus(other: TokenResult): TokenResult = TokenResult(
        tokens = tokens + other.tokens,
        extraTokens = extraTokens + other.extraTokens,
        japaneseTokens = japaneseTokens + other.japaneseTokens,
        japaneseExtraTokens = japaneseExtraTokens + other.japaneseExtraTokens,
        romajiTokens = romajiTokens + other.romajiTokens
    )

    data class Token(
        val value: String,
        val index: Int
    )

    companion object {
        private const val TOKEN_BASIC_WEIGHTING = "primary"
        private const val TOKEN_EXTRA_WEIGHTING = "extra"
        private const val TOKEN_JAPANESE_WEIGHTING = "japanese"
        private const val TOKEN_ROMAJI_WEIGHTING = "romaji"
        private const val TOKEN_JAPANESE_EXTRA_WEIGHTING = "japaneseExtra"

        internal val Empty = TokenResult(
            tokens = emptySet(),
            extraTokens = emptySet(),
            japaneseTokens = emptySet(),
            japaneseExtraTokens = emptySet(),
            romajiTokens = emptySet()
        )

        internal val tokenWeights = mapOf(
            TOKEN_BASIC_WEIGHTING to 1.0,
            TOKEN_JAPANESE_WEIGHTING to 0.95,
            TOKEN_ROMAJI_WEIGHTING to 0.8,
            TOKEN_EXTRA_WEIGHTING to 0.6,
            TOKEN_JAPANESE_EXTRA_WEIGHTING to 0.3
        )
    }
}
