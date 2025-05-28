package dev.datlag.mimasu.extension.matcher.wanakana.utils

internal data class State(
    val tree: MappingTree,
    val convertEnding: Boolean
) {
    var subTree: MappingTree? = tree
        private set

    private var original: String = ""
    private var previousValue: String? = null

    fun nextSubTree(nextChar: Char) {
        original += nextChar
        when (val subTree = this.subTree) {
            null -> previousValue = null
            else -> {
                this.subTree = subTree[nextChar]
                previousValue = subTree.value
            }
        }
    }

    fun isAtTreeEnd(): Boolean = subTree?.hasSubTree() != true

    fun getCurrentValue(rollbackOne: Boolean = false): String {
        val currentOrPrevious = subTree?.value ?: previousValue
        return when {
            currentOrPrevious != null -> currentOrPrevious
            rollbackOne -> original.dropLast(1)
            else -> original
        }
    }

    fun reset() {
        subTree = tree
        original = ""
        previousValue = null
    }

    fun newChunk(remaining: String, currentCursor: Int): List<ConversionToken> {
        if (remaining.isEmpty()) {
            return emptyList()
        }

        val firstChar = remaining[0]
        reset()
        nextSubTree(firstChar)

        return parse(
            remaining = remaining.drop(1),
            lastCursor = currentCursor,
            currentCursor = currentCursor + 1
        )
    }

    private fun parse(
        remaining: String,
        lastCursor: Int,
        currentCursor: Int
    ): List<ConversionToken> {
        if (remaining.isEmpty()) {
            if (convertEnding || isAtTreeEnd()) {
                val kana: String = getCurrentValue()
                return listOf(ConversionToken(lastCursor, currentCursor, kana))
            }

            return listOf(ConversionToken(lastCursor, currentCursor, null))
        }

        if (isAtTreeEnd()) {
            val value = getCurrentValue()
            val token = ConversionToken(lastCursor, currentCursor, value)

            return listOf(token) + newChunk(remaining, currentCursor)
        }

        nextSubTree(remaining[0])
        val nextSubTree = subTree
        if (nextSubTree == null) {
            val token = ConversionToken(lastCursor, currentCursor, getCurrentValue(true))
            return listOf(token) + newChunk(remaining, currentCursor)
        }

        return parse(remaining.drop(1), lastCursor, currentCursor + 1)
    }
}