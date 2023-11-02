package dev.datlag.burningseries.ui.screen.initial.home

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Home

@Parcelize
sealed class HomeConfig : Parcelable {

    @Parcelize
    data class Series(
        val title: String,
        val href: String,
        val coverHref: String?,
    ) : HomeConfig(), Parcelable {
        constructor(series: Home.Series) : this(series.title, series.href, series.coverHref)
        constructor(episode: Home.Episode) : this(episode.series ?: episode.title, episode.href, episode.coverHref)
    }
}