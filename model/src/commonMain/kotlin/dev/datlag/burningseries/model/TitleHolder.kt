package dev.datlag.burningseries.model

import dev.datlag.burningseries.model.algorithm.JaroWinkler

/**
 * Cannot be applied to Database Series object
 */
abstract class TitleHolder {
    abstract val title: String

    val allTitles by lazy {
        title.split('|').filterNot { it.isBlank() }.map { it.trim() }.distinct()
    }

    val bestTitle by lazy {
        when {
            allTitles.size <= 1 -> allTitles.firstOrNull() ?: title
            else -> {
                val newTitles = mutableListOf<String>()
                allTitles.forEach { str ->
                    val strFlatten = str.replace("\\s".toRegex(RegexOption.MULTILINE), String()).trim()

                    if (newTitles.none {
                        JaroWinkler.distance(str, it) > 0.95 || run {
                            val itFlatten = it.replace("\\s".toRegex(RegexOption.MULTILINE), String()).trim()

                            JaroWinkler.distance(strFlatten, itFlatten) > 0.95
                        }
                    }) {
                        newTitles.add(str)
                    }
                }
                newTitles.toSet().joinToString(separator = " | ")
            }
        }
    }

    val mainTitle by lazy {
        bestTitle.substringBefore('|').trim()
    }

    val subTitle by lazy {
        bestTitle.substringAfter('|', missingDelimiterValue = "").trim().ifBlank { null }
    }
}