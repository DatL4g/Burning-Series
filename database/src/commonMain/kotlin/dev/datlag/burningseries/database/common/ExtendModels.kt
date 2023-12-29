package dev.datlag.burningseries.database.common

import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.database.Hoster
import dev.datlag.burningseries.database.SearchItem
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.model.BSUtil
import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.algorithm.JaroWinkler
import dev.datlag.burningseries.model.common.getDigitsOrNull
import dev.datlag.burningseries.model.Series as ModelSeries
import dev.datlag.burningseries.model.Series.Episode as ModelEpisode
import dev.datlag.burningseries.model.Series.Episode.Hoster as ModelHoster

fun Series.toModelSeries(episodes: List<Episode>, hosters: List<Hoster>): ModelSeries {
    return ModelSeries(
        title = this.title,
        description = "",
        coverHref = this.coverHref,
        href = this.href,
        seasonTitle = "",
        selectedLanguage = null,
        seasons = emptyList(),
        languages = emptyList(),
        episodes = episodes.map { it.toModelEpisode(hosters) }
    )
}

fun Episode.toModelEpisode(hosters: List<Hoster>): ModelEpisode {
    return ModelEpisode(
        number = this.number,
        title = this.title,
        href = this.href,
        hosters = hosters.map { it.toModelHoster() }
    )
}

fun Hoster.toModelHoster(): ModelHoster {
    return ModelHoster(
        title = this.title,
        href = this.href
    )
}

fun Genre.toSearchItems(): List<SearchItem> {
    return this.items.map {
        SearchItem(
            href = BSUtil.normalizeHref(it.href).trim(),
            title = it.title.trim(),
            genre = this.title.trim()
        )
    }
}

fun List<SearchItem>.toGenres(): List<Genre> {
    return this.groupBy { it.genre.trim() }.map { (k, v) ->
        Genre(
            title = k.trim(),
            items = v.map {
                Genre.Item(
                    title = it.title.trim(),
                    href = it.href.trim()
                )
            }
        )
    }
}

val Series.allTitles
    get() = allTitlesCache.getOrPut(this) {
        title.split('|').filterNot { it.isBlank() }.map { it.trim() }.distinct()
    }

val Series.bestTitle
    get() = bestTitleCache.getOrPut(this) {
        when {
            allTitles.size <= 1 -> allTitles.firstOrNull() ?: title
            else -> {
                val newTitles = mutableListOf<String>()
                allTitles.forEach { str ->
                    val strFlatten = str.replace("\\s".toRegex(RegexOption.MULTILINE), String()).trim()

                    if (newTitles.none {
                        JaroWinkler.distance(str, it) > 0.95 || run {
                            val itFlatten = it.replace("\\s".toRegex(RegexOption.MULTILINE), String())

                            JaroWinkler.distance(strFlatten, itFlatten) > 0.95F
                        }
                    }) {
                        newTitles.add(str)
                    }
                }
                newTitles.toSet().joinToString(separator = " | ")
            }
        }
    }

private val allTitlesCache = mutableMapOf<Series, List<String>>()
private val bestTitleCache = mutableMapOf<Series, String>()

val Episode.convertedNumber: Int?
    get() = this.number.toIntOrNull() ?: this.number.getDigitsOrNull()?.toIntOrNull()