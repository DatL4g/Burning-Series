package dev.datlag.burningseries.ui.screen.initial

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
sealed class View : Parcelable {

    @Parcelize
    data object Home : View(), Parcelable
}