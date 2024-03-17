package dev.datlag.burningseries.shared.ui.screen.initial.home

import dev.datlag.burningseries.model.Genre
import dev.datlag.burningseries.model.Home
import kotlinx.serialization.Serializable

@Serializable
sealed class HomeConfig {

    @Serializable
    data class Series(
        val title: String?,
        val href: String,
        val coverHref: String?,
    ) : HomeConfig() {
        constructor(series: Home.Series) : this(series.title, series.href, series.coverHref)
        constructor(episode: Home.Episode) : this(episode.series ?: episode.fullTitle, episode.href, episode.coverHref)
        constructor(item: Genre.Item) : this(item.title, item.href, null)
    }
}