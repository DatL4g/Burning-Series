package dev.datlag.burningseries.ui.navigation

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.SeriesInitialInfo
import dev.datlag.burningseries.model.VideoStream

@Parcelize
sealed class ScreenConfig : Parcelable {
    object Login : ScreenConfig()

    object Home : ScreenConfig()

    object Genre : ScreenConfig()

    data class Series(
        val href: String,
        val initialInfo: SeriesInitialInfo,
        val isEpisode: Boolean,
        val continueWatching: Boolean
    ) : ScreenConfig()

    data class Video(
        val series: dev.datlag.burningseries.model.Series,
        val episode: dev.datlag.burningseries.model.Series.Episode,
        val streams: List<VideoStream>
    ) : ScreenConfig()

    data class Activate(
        val series: dev.datlag.burningseries.model.Series,
        val episode: dev.datlag.burningseries.model.Series.Episode
    ) : ScreenConfig()

    object About : ScreenConfig()

    object Settings : ScreenConfig()

    object Favorites : ScreenConfig()
}
