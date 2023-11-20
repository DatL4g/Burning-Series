package dev.datlag.burningseries.ui.navigation

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import dev.datlag.burningseries.model.Series
import dev.datlag.burningseries.model.Stream

@Parcelize
sealed class ScreenConfig : Parcelable {

    @Parcelize
    data object Home : ScreenConfig(), Parcelable

    @Parcelize
    data class Video(val streams: List<Stream>) : ScreenConfig(), Parcelable
}