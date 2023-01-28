package dev.datlag.burningseries.other

import com.arkivanov.essenty.parcelable.ParcelableContainer

object StateSaver {

    var homeSeriesViewPos: Int = 0
    var homeSeriesViewOffset: Int = 0

    var homeEpisodeViewPos: Int = 0
    var homeEpisodeViewOffset: Int = 0

    var genreViewPos: Int = 0
    var genreViewOffset: Int = 0

    var settingsViewPos: Int = 0
    var settingsViewOffset: Int = 0

    var seriesViewPos: Int = 0
    var seriesViewOffset: Int = 0

    var favoriteViewPos: Int = 0
    var favoriteViewOffset: Int = 0

    var webviewScrollX: Int = 0
    var webviewScrollY: Int = 0

    val state: MutableMap<String, ParcelableContainer> = mutableMapOf()
}