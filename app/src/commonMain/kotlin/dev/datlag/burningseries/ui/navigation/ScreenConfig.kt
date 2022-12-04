package dev.datlag.burningseries.ui.navigation

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.SeriesInitialInfo

@Parcelize
sealed class ScreenConfig() : Parcelable {
    object Login : ScreenConfig()

    object Home : ScreenConfig()

    object Genre : ScreenConfig()

    data class Series(
        val href: String,
        val initialInfo: SeriesInitialInfo
    ) : ScreenConfig()
}
