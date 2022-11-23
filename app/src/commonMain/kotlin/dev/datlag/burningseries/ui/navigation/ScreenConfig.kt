package dev.datlag.burningseries.ui.navigation

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
sealed class ScreenConfig() : Parcelable {
    object Login : ScreenConfig()
}
