package dev.datlag.burningseries.database.common

import dev.datlag.burningseries.database.Episode
import dev.datlag.burningseries.model.Series.Episode as ModelEpisode
import dev.datlag.burningseries.database.Series
import dev.datlag.burningseries.model.Series as ModelSeries

fun Series.toModelSeries(episodes: List<Episode>): ModelSeries {
    return ModelSeries(
        title = this.title,
        description = "",
        coverHref = this.coverHref,
        href = this.href,
        seasonTitle = "",
        selectedLanguage = null,
        seasons = emptyList(),
        languages = emptyList(),
        episodes = episodes.map { it.toModelEpisode() }
    )
}

fun Episode.toModelEpisode(): ModelEpisode {
    return ModelEpisode(
        number = this.number,
        title = this.title,
        href = this.href,
        hosters = emptyList()
    )
}