package dev.datlag.burningseries.ui.screen.initial.home

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

@Parcelize
sealed class HomeConfig : Parcelable {

    @Parcelize
    data object Series : HomeConfig(), Parcelable
}