package dev.datlag.mimasu.extension.matcher

import dev.datlag.mimasu.extension.matcher.distance.JaroWinkler

data object SearchMatcher {

    fun calculateSymmetricSimilarity(first: TokenResult, second: TokenResult): Double {
        val scoreAtoB = calculateSimilarity(first, second)
        val scoreBtoA = calculateSimilarity(second, first)

        return ((scoreAtoB + scoreBtoA) / 2.0).coerceIn(0.0, 1.0)
    }

    fun calculateSimilarity(first: TokenResult, second: TokenResult): Double {
        var totalScore = CalculatedCategory(achieved = 0.0, maxPossible = 0.0)

        totalScore += calculateCategoryScore(
            firstTokens = first.tokens,
            secondTokens = second.tokens,
            categoryWeight = TokenResult.primaryTokenWeight
        )
        totalScore += calculateCategoryScore(
            firstTokens = first.japaneseTokens,
            secondTokens = second.japaneseTokens,
            categoryWeight = TokenResult.japaneseTokenWeight
        )
        totalScore += calculateCategoryScore(
            firstTokens = first.romajiTokens,
            secondTokens = second.romajiTokens,
            categoryWeight = TokenResult.romajiTokenWeight
        )
        totalScore += calculateCategoryScore(
            firstTokens = first.extraTokens,
            secondTokens = second.extraTokens,
            categoryWeight = TokenResult.extraTokenWeight
        )
        totalScore += calculateCategoryScore(
            firstTokens = first.japaneseExtraTokens,
            secondTokens = second.japaneseExtraTokens,
            categoryWeight = TokenResult.japaneseExtraTokenWeight
        )

        return if (totalScore.maxPossible > 0.0) {
            (totalScore.achieved / totalScore.maxPossible).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
    }

    private fun calculateCategoryScore(
        firstTokens: Collection<TokenResult.Token>,
        secondTokens: Collection<TokenResult.Token>,
        categoryWeight: Double
    ): CalculatedCategory {
        if (firstTokens.isEmpty() || secondTokens.isEmpty()) {
            return CalculatedCategory(achieved = 0.0, maxPossible = 0.0)
        }

        var currentCategoryScore = 0.0
        val secondTokenValues = secondTokens.map { it.value.lowercase() }

        for (firstToken in firstTokens) {
            val firstValue = firstToken.value.lowercase()
            var bestMatchScore = 0.0

            for (secondToken in secondTokenValues) {
                val similarity = JaroWinkler.Default.similarity(firstValue, secondToken)
                if (similarity > bestMatchScore) {
                    bestMatchScore = similarity
                }
            }

            if (bestMatchScore > 0.7) {
                currentCategoryScore += bestMatchScore
            }
        }

        val maxPossibleForCategory = firstTokens.size * categoryWeight
        val achievedForCategory = currentCategoryScore * categoryWeight

        return CalculatedCategory(
            achieved = achievedForCategory,
            maxPossible = maxPossibleForCategory
        )
    }

    private data class CalculatedCategory(
        val achieved: Double,
        val maxPossible: Double
    ) {

        operator fun plus(other: CalculatedCategory) = CalculatedCategory(
            achieved = achieved + other.achieved,
            maxPossible = maxPossible + other.maxPossible
        )
    }

}