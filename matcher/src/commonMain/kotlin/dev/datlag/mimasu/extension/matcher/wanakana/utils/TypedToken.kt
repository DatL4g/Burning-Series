package dev.datlag.mimasu.extension.matcher.wanakana.utils

internal data class TypedToken(
    val value: String,
    val type: TokenType
) {
    companion object {
        fun tokenize(input: String, compact: Boolean = false): List<TypedToken> {
            if (input.isEmpty()) {
                return emptyList()
            }

            var currentValue: String
            var currentType: TokenType

            input.first().let { firstChar ->
                currentValue = firstChar.toString()
                currentType = TokenType.from(firstChar, compact)
            }

            val result = mutableListOf<TypedToken>()
            input.drop(1).forEach { char ->
                val type = TokenType.from(char, compact)
                if (type == currentType) {
                    currentValue += char
                } else {
                    result.add(TypedToken(currentValue, currentType))
                    currentValue = char.toString()
                    currentType = type
                }
            }

            result.add(TypedToken(currentValue,currentType))
            return result
        }
    }
}
